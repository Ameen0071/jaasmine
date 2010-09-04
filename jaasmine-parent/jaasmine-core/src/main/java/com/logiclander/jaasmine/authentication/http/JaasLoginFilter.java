/*
 * Copyright 2010 LogicLander
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.logiclander.jaasmine.authentication.http;

import com.logiclander.jaasmine.authentication.AuthenticationService;
import com.logiclander.jaasmine.authentication.SimpleAuthenticationService;
import java.io.IOException;
import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Checks incoming ServletRequests and ServletResponses for authentication.
 *
 * This filter accepts the following init-params:
 * <UL>
 *  <LI>appName - the name of the application in the JAAS configuration.  This
 * parameter is optional.  The default value is
 * {@value AuthenticationService#DEFAULT_JAAS_SPNEGO_CONFIG}</LI>
 *  <LI>loginServletName - the name of the Servlet that will be used to
 * collect user credentials.  This parameter is optional.  The default value is
 * {@value #DEFAULT_NAMED_LOGIN_DISPATCHER}</LI>
 * </UL>
 *
 * Requests that invoke this Filter must have parameters named {@code username}
 * and {@code password} set, otherwise the request cannot be processed.  This
 * Filter processes logins using the {@link SimpleAuthenticationService}.
 *
 * Instances of this class have a configurable commons-logging based logger
 * named
 * {@code com.logiclander.jaasmine.authentication.http.JaasLoginFilter}.
 */
public class JaasLoginFilter implements Filter {


    /** The logger for this instance. */
    private transient final Log logger =
            LogFactory.getLog(JaasLoginFilter.class);


    /** The default value for the appName, which is {@value}.*/
    private static final String DEFAULT_NAMED_LOGIN_DISPATCHER =
            "JaasLoginServlet";


    /**
     * The application name for the configuration to use in the JAAS file.  The
     * default value is
     * {@value AuthenticationService#DEFAULT_JAAS_SPNEGO_CONFIG}.
     */
    private String appName;


    /**
     * The name of the Servlet to use for post login processing.  The default
     * value is {@value #DEFAULT_NAMED_LOGIN_DISPATCHER}.
     */
    private String loginServletName;


    /**
     * {@inheritDoc}
     *
     * Checks the given FilterConfig for the init-params named appName and
     * loginServletName.  If these values are not in the FilterConfig, then
     * the default values are used.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        appName = filterConfig.getInitParameter("appName");
        if (appName == null || appName.isEmpty()) {
            appName = AuthenticationService.DEFAULT_JAAS_SPNEGO_CONFIG;
        }

        loginServletName = filterConfig.getInitParameter("loginServletName");
        if (loginServletName == null || loginServletName.isEmpty()) {
            loginServletName = DEFAULT_NAMED_LOGIN_DISPATCHER;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("%s initialized", toString()));
        }

    }


    /**
     * This implementation will filter requests for credentials and determine if
     * processing of the FilterChain can proceed.  Filtering occurs as follows:
     * <OL>
     *  <LI>If the request is not an HttpServletRequest and the response is not
     * an HttpServletResponse, continue processing the filter chain (this almost
     * never happens)</LI>
     *  <LI>The HttpSession is checked for an attribute named
     * {@link AuthenticationService#SUBJECT_KEY AuthenticationService.SUBJECT_KEY}</LI>
     *  <LI>If found, then processing the filter chain continues.</LI>
     *  <LI>If not found, then the request is checked for the {@code username}
     * and {@code password} parameters.  If these parameters are present, then
     * the SimpleAuthenticationService's login method is invoked with those
     * credentials.</LI>
     *  <LI>If a Subject is returned, it is saved to the HttpSession with the
     * key from above.</LI>
     *  <LI>If a Subject is not returned, the filter will dispatch to a Servlet
     * configured in the {@code web.xml} for the web application with the
     * servlet-name {@code JaasLoginServlet}.</LI>
     * </OL>
     *
     * @param request the ServletRequest
     * @param response the ServletResponse
     * @param chain the FilterChain
     * @throws IOException if an I/O error occurs in the FilterChain
     * @throws ServletException if a processing error occurs in the FilterChain
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
            FilterChain chain) throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) &&
            !(response instanceof HttpServletResponse)) {

            chain.doFilter(request, response);

        } else {

            HttpServletRequest httpReq = (HttpServletRequest) request;
            HttpServletResponse httpResp = (HttpServletResponse) response;

            Exception exception = null;

            try {

                boolean canExecute = hasCredentials(httpReq);

                if (!canExecute) {
                    canExecute = login(httpReq);
                }

                if (canExecute) {

                    chain.doFilter(httpReq, httpResp);

                } else {

                    RequestDispatcher loginDispatcher =
                        httpReq.getSession()
                            .getServletContext()
                            .getNamedDispatcher(loginServletName);

                    if (loginDispatcher != null) {

                        loginDispatcher.forward(httpReq, httpResp);
                        return;

                    } else {

                        String msg =
                            String.format("Servlet %s is not configured",
                                loginServletName);
                        throw new ServletException(msg);

                    }
                    
                }

            } catch (IOException ex) {

                exception = ex;
                throw(ex);

            } catch (ServletException ex) {

                exception = ex;
                throw(ex);

            } finally {

                if (exception != null) {

                    if (logger.isErrorEnabled()) {
                        String msg =
                            String.format("Caught exception in filter chain: %s",
                                exception.getMessage());
                        logger.error(msg, exception);
                    }
                }

            }
        }
    }


    /**
     * Writes a log message using the configured logger at DEBUG level stating
     * that the Filter is destroyed.
     */
    @Override
    public void destroy() {

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("%s destroyed", toString()));
        }

    }


    /**
     * @return the String representation of this JaasLoginFilter.
     */
    @Override
    public String toString() {
        return String.format("%s for %s", this.getClass().getSimpleName(),
                appName);
    }


    /**
     * @param req an HttpServletRequest
     * @return true if the credentials are present on the request.
     */
    private boolean hasCredentials(HttpServletRequest req) {

        return hasSubject(req);

    }


    /**
     * @param req an HttpServletRequest
     * @return true if the Subject is found on the request.
     */
    private boolean hasSubject(HttpServletRequest req) {

        HttpSession sess = req.getSession(false);
        if (sess == null) {
            return false;
        }

        Subject subj =
            (Subject) sess.getAttribute(AuthenticationService.SUBJECT_KEY);

        return (subj != null);
    }


    /**
     * @param request the HttpServletRequest.
     * @return true if the Subject is obtained from the SimpleLoginService.
     */
    private boolean login(HttpServletRequest request) {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        boolean subjectObtained = false;

        if (username == null || username.isEmpty()) {

            if (logger.isDebugEnabled()) {
                logger.debug("username is missing");
            }
            return subjectObtained;

        }

        if (password == null || password.isEmpty()) {

            if (logger.isDebugEnabled()) {
                logger.debug("password is missing");
            }
            return subjectObtained;
        }

        AuthenticationService as = new SimpleAuthenticationService(appName);
        Subject s = as.login(username, password.toCharArray());
        subjectObtained = (s != null);

        if (subjectObtained) {

            // Assuming that if we got here, we need to create the session
            HttpSession sess = request.getSession();
            sess.setAttribute(AuthenticationService.SUBJECT_KEY, s);
        }
        
        return subjectObtained;
    }
}