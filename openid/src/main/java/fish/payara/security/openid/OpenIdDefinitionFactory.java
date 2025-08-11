/*
 * Copyright (c) 2025 Payara Foundation and/or its affiliates. All rights reserved.
 *
 *  The contents of this file are subject to the terms of either the GNU
 *  General Public License Version 2 only ("GPL") or the Common Development
 *  and Distribution License("CDDL") (collectively, the "License").  You
 *  may not use this file except in compliance with the License.  You can
 *  obtain a copy of the License at
 *  https://github.com/payara/Payara/blob/master/LICENSE.txt
 *  See the License for the specific
 *  language governing permissions and limitations under the License.
 *
 *  When distributing the software, include this License Header Notice in each
 *  file and include the License file at glassfish/legal/LICENSE.txt.
 *
 *  GPL Classpath Exception:
 *  The Payara Foundation designates this particular file as subject to the "Classpath"
 *  exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 *  file that accompanied this code.
 *
 *  Modifications:
 *  If applicable, add the following below the License Header, with the fields
 *  enclosed by brackets [] replaced by your own identifying information:
 *  "Portions Copyright [year] [name of copyright owner]"
 *
 *  Contributor(s):
 *  If you wish your version of this file to be governed by only the CDDL or
 *  only the GPL Version 2, indicate your decision by adding "[Contributor]
 *  elects to include this software in this distribution under the [CDDL or GPL
 *  Version 2] license."  If you don't indicate a single choice of license, a
 *  recipient has the option to distribute your version of this file under
 *  either the CDDL, the GPL Version 2 or to extend the choice of license to
 *  its licensees as provided above.  However, if you add GPL Version 2 code
 *  and therefore, elected the GPL Version 2 license, then the option applies
 *  only if the new code is made subject to such option by the copyright
 *  holder.
 */
package fish.payara.security.openid;

import fish.payara.security.annotations.ClaimsDefinition;
import fish.payara.security.annotations.LogoutDefinition;
import fish.payara.security.annotations.OpenIdAuthenticationDefinition;
import fish.payara.security.annotations.OpenIdProviderMetadata;
import fish.payara.security.annotations.ProxyDefinition;
import fish.payara.security.openid.api.DisplayType;
import fish.payara.security.openid.api.OpenIdConstant;
import fish.payara.security.openid.api.PromptType;
import java.lang.annotation.Annotation;
import org.eclipse.microprofile.config.Config;

/**
 *
 * @author Gaurav Gupta
 */
public class OpenIdDefinitionFactory {
    
    public static final String OPENID_ENABLED = "payara.security.openid";

    public static OpenIdAuthenticationDefinition fromConfig(Config config) {
        Boolean oidcEnabled = config.getOptionalValue(OPENID_ENABLED, Boolean.class).orElse(false);
        if (!oidcEnabled) {
            return null;
        }
        return new OpenIdAuthenticationDefinition() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return OpenIdAuthenticationDefinition.class;
            }

            @Override
            public String providerURI() {
                return "";
            }

            @Override
            public String clientId() {
                return "";
            }

            @Override
            public String clientSecret() {
                return "";
            }

            @Override
            public String redirectURI() {
                return "${baseURL}/Callback";
            }

            @Override
            public String[] scope() {
                return new String[]{
                    OpenIdConstant.OPENID_SCOPE,
                    OpenIdConstant.EMAIL_SCOPE,
                    OpenIdConstant.PROFILE_SCOPE
                };
            }

            @Override
            public String responseType() {
                return "code";
            }

            @Override
            public String responseMode() {
                return "";
            }

            @Override
            public PromptType[] prompt() {
                return new PromptType[0];
            }

            @Override
            public DisplayType display() {
                return DisplayType.PAGE;
            }

            @Override
            public boolean useNonce() {
                return true;
            }

            @Override
            public boolean useSession() {
                return true;
            }

            @Override
            public String[] extraParameters() {
                return new String[0];
            }

            @Override
            public int jwksConnectTimeout() {
                return 500;
            }

            @Override
            public int jwksReadTimeout() {
                return 500;
            }

            @Override
            public boolean tokenAutoRefresh() {
                return false;
            }

            @Override
            public int tokenMinValidity() {
                return 10_000;
            }

            @Override
            public boolean userClaimsFromIDToken() {
                return false;
            }

            @Override
            public OpenIdProviderMetadata providerMetadata() {
                return new OpenIdProviderMetadata() {
                    @Override
                    public String issuer() {
                        return "";
                    }

                    @Override
                    public String authorizationEndpoint() {
                        return "";
                    }

                    @Override
                    public String tokenEndpoint() {
                        return "";
                    }

                    @Override
                    public String userinfoEndpoint() {
                        return "";
                    }

                    @Override
                    public String endSessionEndpoint() {
                        return "";
                    }

                    @Override
                    public String jwksURI() {
                        return "";
                    }

                    @Override
                    public String[] scopesSupported() {
                        return new String[0];
                    }

                    @Override
                    public String[] responseTypesSupported() {
                        return new String[0];
                    }

                    @Override
                    public String[] subjectTypesSupported() {
                        return new String[0];
                    }

                    @Override
                    public String[] idTokenSigningAlgValuesSupported() {
                        return new String[0];
                    }

                    @Override
                    public String[] idTokenEncryptionAlgValuesSupported() {
                        return new String[0];
                    }

                    @Override
                    public String[] idTokenEncryptionEncValuesSupported() {
                        return new String[0];
                    }

                    @Override
                    public String[] claimsSupported() {
                        return new String[0];
                    }

                    @Override
                    public boolean disableScopeValidation() {
                        return false;
                    }

                    @Override
                    public String accessTokenIssuer() {
                        return "";
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return OpenIdProviderMetadata.class;
                    }
                };
            }

            @Override
            public ClaimsDefinition claimsDefinition() {
                return new ClaimsDefinition() {
                    @Override
                    public String callerNameClaim() {
                        return OpenIdConstant.PREFERRED_USERNAME;
                    }

                    @Override
                    public String callerGroupsClaim() {
                        return OpenIdConstant.GROUPS;
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return ClaimsDefinition.class;
                    }
                };
            }

            @Override
            public ProxyDefinition proxyDefinition() {
                return new ProxyDefinition() {
                    @Override
                    public String hostName() {
                        return "";
                    }

                    @Override
                    public String port() {
                        return "";
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return ProxyDefinition.class;
                    }

                };
            }

            @Override
            public LogoutDefinition logout() {
                return new LogoutDefinition() {
                    @Override
                    public boolean notifyProvider() {
                        return false;
                    }

                    @Override
                    public String redirectURI() {
                        return "";
                    }

                    @Override
                    public boolean accessTokenExpiry() {
                        return false;
                    }

                    @Override
                    public boolean identityTokenExpiry() {
                        return false;
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return LogoutDefinition.class;
                    }
                };
            }
        };
    }

}
