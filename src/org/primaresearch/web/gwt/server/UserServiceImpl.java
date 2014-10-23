/*
 * Copyright 2014 PRImA Research Lab, University of Salford, United Kingdom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primaresearch.web.gwt.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.primaresearch.web.gwt.client.user.UserService;
import org.primaresearch.web.gwt.shared.RemoteException;
import org.primaresearch.web.gwt.shared.user.Permissions;
import org.primaresearch.web.gwt.shared.user.SessionData;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.google.gson.Gson;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Exchange of user related data.
 * 
 * @author Christian Clausner
 *
 */
public class UserServiceImpl extends RemoteServiceServlet implements UserService {

	private static final long serialVersionUID = 1L;
	
	private static final long AUTHENTICATION_TIMEOUT = 60000L; //One minute
	
	private boolean DEBUG = false;
	
	private String databaseUrl;
	private String databaseClass = "com.mysql.jdbc.Driver"; 
	private String databaseUser;
	private String databasePass;
	private String encryptionCharEncoding = "ISO-8859-1";
	private String staticSecretKey = null;
	private String staticIntegrationServiceUrl = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		//Parameters defined in web.xml:
		if (getInitParameter("DEBUG_MODE") != null)
			DEBUG = Boolean.parseBoolean(getInitParameter("DEBUG_MODE"));
		if (DEBUG)
			System.out.println(">> Debug mode <<");

		if (getInitParameter("DATABASE_URL") != null)
			databaseUrl = getInitParameter("DATABASE_URL");

		if (getInitParameter("DATABASE_CLASS") != null)
			databaseClass = getInitParameter("DATABASE_CLASS");

		if (getInitParameter("DATABASE_USER") != null)
			databaseUser = getInitParameter("DATABASE_USER");

		if (getInitParameter("DATABASE_PASS") != null)
			databasePass = getInitParameter("DATABASE_PASS");

		if (getInitParameter("ENCRYPTION_CHAR_ENCODING") != null)
			encryptionCharEncoding = getInitParameter("ENCRYPTION_CHAR_ENCODING");
		
		if (getInitParameter("STATIC_SECRET_KEY") != null && !getInitParameter("STATIC_SECRET_KEY").isEmpty())
			staticSecretKey = getInitParameter("STATIC_SECRET_KEY");
		
		if (getInitParameter("STATIC_SOAP_URL") != null && !getInitParameter("STATIC_SOAP_URL").isEmpty())
			staticIntegrationServiceUrl = getInitParameter("STATIC_SOAP_URL");
		
	}
	
	@Override
	public SessionData logOn(String applicationId, String documentId, String attachmentId, String userData) throws RemoteException {
		
		if (DEBUG)
			System.out.println("Logging on: "+applicationId + ", " + documentId + ", " + attachmentId + ", " + userData);
		
		if (userData == null || userData.isEmpty())
			return null;

		try {
		
			//Get session
			HttpServletRequest request = this.getThreadLocalRequest();
			HttpSession session = request.getSession();
			if (DEBUG) {
				System.out.println(" HttpServletRequest:");
				System.out.println("   Server name: "+this.getThreadLocalRequest().getServerName());
				System.out.println("   Server port: "+this.getThreadLocalRequest().getServerPort());
				System.out.println("   Scheme: "+this.getThreadLocalRequest().getScheme());
				System.out.println("   Remote host: "+this.getThreadLocalRequest().getRemoteHost());
				System.out.println("   Remote port: "+this.getThreadLocalRequest().getRemotePort());
				System.out.println("   Request URI: "+this.getThreadLocalRequest().getRequestURI());
			}
	
			//Get application data from database
			if (DEBUG)
				System.out.println("  Getting app data from databse");
			ApplicationData appData = getAppDataFromDatabase(applicationId);
			session.setAttribute(SessionAttributes.SOAP_SERVICE, appData.integrationServiceUrl);
			
			//Decrypt
			if (DEBUG)
				System.out.println("  Decrypting user token");
			userData = decrypt(userData, appData.secretKey);
			
			//Deserialise JSON object
			if (DEBUG)
				System.out.println("  Reading data from JSON user token\n"+userData);
			Gson gson = new Gson();
			UserToken token = gson.fromJson(userData, UserToken.class);
			
			if (	token.ip == null || token.ip.isEmpty()
				 ||	token.ts == null || token.ts.isEmpty()
				 ||	token.uid == null || token.uid.isEmpty())
			{
				return null;
			}
			
			//Authenticate
			// Check IP address
			String remote = request.getRemoteAddr();
			//String local = request.getLocalAddr();
			if (!remote.equals(token.ip) && (!"127.0.0.1".equals(remote) || !DEBUG))
			{
				if (DEBUG)
					System.out.println("  IP addresses do not match: In token: '"+token.ip + "'; client: '"+remote+"'");
				return null;
			}
			
			// Check time stamp
			try {
				Date timestamp = new Date(Long.parseLong(token.ts)*1000L);
				Date now = new Date();
				
				if (now.getTime() - timestamp.getTime() > AUTHENTICATION_TIMEOUT)
				{
					if (DEBUG)
						System.out.println("  Token timed out");
					return null;
				}
			} catch (NumberFormatException e) {
				if (DEBUG)
					System.out.println("  NumberFormatException for timestamp in user token");
				//e.printStackTrace();
				return null;
			}
			
			//Mark session as authentic (set flag)
			Boolean userAuthenticated = true;
			session.setAttribute(SessionAttributes.USER_AUTH, userAuthenticated);
			session.setAttribute(SessionAttributes.USER_ID, token.uid);
			
			//Attachment ID
			session.setAttribute(SessionAttributes.ATTACHMENT_ID, attachmentId);
	
			//Get all relevant web service URLs from the integration web service
			if (DEBUG)
				System.out.println("  Getting web service URLs from integration service");
			WebServiceInfo webServices = getWebServiceInfo(appData.integrationServiceUrl, token.uid, attachmentId);
			session.setAttribute(SessionAttributes.PAGE_CONTENT_WEB_SERVICE, webServices.pageContentWebService);
			
			//Request list of rights from database
			if (DEBUG)
				System.out.println("  Getting permissions");
			Permissions permissions = getPermissions(appData.integrationServiceUrl, token.uid, attachmentId);
			session.setAttribute(SessionAttributes.PERMISSIONS, userAuthenticated);
	
			//Return value
			SessionData sessionData = new SessionData();
			sessionData.getDocumentImageUrl = webServices.imageWebService;
			sessionData.permissions = permissions;
			
			if (DEBUG)
				System.out.println("Done");
			return sessionData;
		} catch (Exception exc) {
			exc.printStackTrace();
			throw new RemoteException("Could not log on: "+exc.getMessage());
		}
	}
	
	/**
	 * Decrypt data (AES encryption) 
	 * @param msgBase64 - Initialisation vector + encrypted data, base64 encoded
	 * @return Decrypted data
	 */
	private String decrypt(String msgBase64, String key) {
		
		final String PHP_CHAR_ENCODING = encryptionCharEncoding;		//Character encoding used for encryption
		final int IV_LENGTH = 16;							//Length of initialisation vector (vector required for encryption/decryption)
		Base64 base64 = new org.apache.commons.codec.binary.Base64();
		
	    String decryptedData = null;
	    try {
	    	byte[] msgBytes = base64.decode(msgBase64.getBytes());	//Decode base64
	    	String m = new String(msgBytes, PHP_CHAR_ENCODING);
		
	    	//Split into initialisation vector and encrypted data
	    	String initialVectorString = m.substring(0, IV_LENGTH);
	    	String encryptedData = m.substring(IV_LENGTH);
	    	
	    	//byte[] initialVectorBytes = initialVectorString.getBytes();
	    	byte[] initialVectorBytes = initialVectorString.getBytes(PHP_CHAR_ENCODING);
	    	byte[] encryptedDataBytes = encryptedData.getBytes(PHP_CHAR_ENCODING);
		
	    	//Decrypt
	    	String md5key = md5(key);
	    	SecretKeySpec skeySpec = new SecretKeySpec(md5key.getBytes(), "AES");
	    	IvParameterSpec initialVector = new IvParameterSpec(initialVectorBytes);
	    	Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
	    	cipher.init(Cipher.DECRYPT_MODE, skeySpec, initialVector);
	    	byte[] decryptedByteArray = cipher.doFinal(encryptedDataBytes);
	    	
	    	decryptedData = new String(decryptedByteArray, PHP_CHAR_ENCODING);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return decryptedData;
	    
	}
	
	/**
	 * Message Digest
	 * @throws NoSuchAlgorithmException
	 */
	private static String md5(String input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] messageDigest;
		messageDigest = md.digest(input.getBytes());
		BigInteger number = new BigInteger(1, messageDigest);
		return String.format("%032x", number);
	}
	
	/**
	 * Retrieves application data from the database (e.g. SOAP service URL and decryption key)
	 * or uses static parameters specified in web.xml.
	 * @param applicationId Web application ID
	 * @return Retrieved data
	 */
	private ApplicationData getAppDataFromDatabase(String applicationId) {
		ApplicationData data = new ApplicationData();
		String integrationServiceUrlHost = null;
		String integrationServiceUrlPath = null;
		
		boolean connectToDatabase = 	staticIntegrationServiceUrl == null
									||	staticSecretKey == null;

		if (connectToDatabase) {
			String dbUrl = databaseUrl;
			String dbClass = databaseClass;
			String query = "Select secretKey,integrationServiceHost,integrationServicePath FROM applications WHERE name='"+applicationId+"'";
	
			try {
	
				Class.forName(dbClass);
				//Connection con = DriverManager.getConnection(dbUrl,"www-user", "Kd7q6wJ6KnL4LLQm");
				Connection con = DriverManager.getConnection(dbUrl, databaseUser, databasePass);
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(query);
	
				while (rs.next()) {
					data.secretKey = rs.getString(1);
					integrationServiceUrlHost = rs.getString(2);
					integrationServiceUrlPath = rs.getString(3);
					break;
				} //end while
	
				con.close();
			} catch(ClassNotFoundException e) {
				e.printStackTrace();
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		
		//Use host from ServletRequest if no host specified in database
		if (integrationServiceUrlHost == null || integrationServiceUrlHost.isEmpty()) {
			integrationServiceUrlHost = this.getThreadLocalRequest().getScheme() + "://" + this.getThreadLocalRequest().getServerName();
		}
		data.integrationServiceUrl = integrationServiceUrlHost+integrationServiceUrlPath;
		
		//Override with if static parameters?
		if (staticSecretKey != null)
			data.secretKey = staticSecretKey;
		
		if (staticIntegrationServiceUrl != null)
			data.integrationServiceUrl = staticIntegrationServiceUrl;
		
		return data;
	}
	
	/**
	 * Calls the integration web service and retrieves the relevant parameters for getting the document image, the
	 * page content and the permissions.
	 */
	private WebServiceInfo getWebServiceInfo(String webServiceUrl, String userId, String attachmentId) {
		WebServiceInfo services = new WebServiceInfo();
		
		//Soap request
		try {
			SimpleSoapRequest request = new SimpleSoapRequest(webServiceUrl, "getDocumentAttachmentSources");
			request.addMethodParameter("Uid", userId);
			request.addMethodParameter("Aid", attachmentId);
			request.setDEBUG(DEBUG);
			
			String soapResponseContent = request.send();

			InputStream is = new ByteArrayInputStream(soapResponseContent.getBytes());
		    
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			
			Node root = doc.getFirstChild();
			if (root != null) {
				Node node = root.getFirstChild();
				while (node != null) {
					if ("DocumentAttachmentSources".equals(node.getNodeName())) {
						
						Node param = node.getFirstChild();
						while (param != null) {
							if ("ImageSource".equals(param.getNodeName()))
								services.imageWebService = param.getTextContent();
							else if ("AttachmentSource".equals(param.getNodeName()))
								services.pageContentWebService = param.getTextContent();
							
							param = param.getNextSibling();
						}
						break;
					}
					node = node.getNextSibling();
				}
			}
	
			is.close();
			
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		return services;
	}
	
	/**
	 * Calls the permission web service and retrieves the user permissions.
	 */
	private Permissions getPermissions(String webServiceUrl, String userId, String attachmentId) {
		
		ArrayList<String> permissionStrings = new ArrayList<String>();

		//Soap request
		try {
			SimpleSoapRequest request = new SimpleSoapRequest(webServiceUrl, "getDocumentAttachmentPermissions");
			request.addMethodParameter("Uid", userId);
			request.addMethodParameter("Aid", attachmentId);
			String soapResponseContent = request.send();

			InputStream is = new ByteArrayInputStream(soapResponseContent.getBytes());

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			
			Node root = doc.getFirstChild();
			if (root != null) {
				Node node = root.getFirstChild();
				while (node != null) {
					if ("DocumentAttachmentPermissions".equals(node.getNodeName())) {

						Node permission = node.getFirstChild();
						while (permission != null) {

							if ("Permission".equals(permission.getNodeName())) {
								NamedNodeMap attrs = node.getAttributes();
								if (attrs != null && attrs.getNamedItem("name") != null) {
									Node attr = attrs.getNamedItem("name");
									permissionStrings.add(attr.getNodeValue());
								}
							}
							permission = permission.getNextSibling();
						}
					}
					node = node.getNextSibling();
				}
			}
			is.close();
			
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
		
		Permissions ret = new Permissions();
		ret.init(permissionStrings.toArray(new String[permissionStrings.size()]));		
		
		return ret;
	}
	
	
	
	/**
	 * Token with user data that is passed from a surrounding website/repository/framework to this web application.
	 * 
	 * @author Christian Clausner
	 *
	 */
	private static class UserToken {
		/** IP Address */
		private String ip = "";
		
		/** Time stamp */
		private String ts = "";
		
		/** User name */
		private String uid = "";
	}
	
	
	/**
	 * Data for the application the current web app (e.g. WebAletheia) is integrated in.
	 * This data is usually stored in a private database.
	 * 
	 * @author Christian Clausner
	 *
	 */
	private static class ApplicationData {
		public String secretKey;
		public String integrationServiceUrl; 
	}

	
	/**
	 * URLs to web services used for integration.
	 * @author Christian Clausner
	 *
	 */
	private static class WebServiceInfo {
		public String imageWebService;
		public String pageContentWebService;
	}
	


}
