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

package com.logiclander.jaasmine.authentication;

import javax.security.auth.Subject;

/**
 * Implementations will authenticate users and return
 * {@link javax.security.auth.Subject Subjects}.
 *
 * @author agherna
 */
public interface AuthenticationService {

    /**
     * A key that can be associated with the Subject returned by
     * {@link #authenticate(java.lang.String, char[])  authenticate}.
     */
    public static final String SUBJECT_KEY =
            "__com.logiclander.jaasmine.authentication.SUBJECT";

    public static final String DEFAULT_JAAS_SPNEGO_CONFIG =
            "jaasmine.spnego.login";

    /**
     * Returns the Subject for the given credentials or null if the login fails
     * fails.
     *
     * @param userId the user's ID
     * @param password the password
     */
    public Subject login(String userId, char[] password);

    
    /**
     * Logout the given Subject
     *
     * @param s the Subject to logout
     */
    public void logout(Subject s);

}
