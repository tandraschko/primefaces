/*
 * The MIT License
 *
 * Copyright (c) 2009-2025 PrimeTek Informatics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primefaces.component.captcha;

import org.primefaces.PrimeFaces;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.Constants;
import org.primefaces.util.MessageFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.context.FacesContext;

import org.json.JSONObject;

@ResourceDependency(library = "primefaces", name = "jquery/jquery.js")
@ResourceDependency(library = "primefaces", name = "core.js")
@ResourceDependency(library = "primefaces", name = "components.js")
@ResourceDependency(library = "primefaces", name = "captcha/captcha.js")
public class Captcha extends CaptchaBase {

    public static final String COMPONENT_TYPE = "org.primefaces.component.Captcha";

    public static final String PUBLIC_KEY = "primefaces.PUBLIC_CAPTCHA_KEY";
    public static final String PRIVATE_KEY = "primefaces.PRIVATE_CAPTCHA_KEY";
    public static final String INVALID_MESSAGE_ID = "primefaces.captcha.INVALID";
    public static final String RECAPTCHA = "g-recaptcha";
    public static final String HCAPTCHA = "h-captcha";

    private static final Logger LOGGER = Logger.getLogger(Captcha.class.getName());

    @Override
    protected void validateValue(FacesContext context, Object value) {
        super.validateValue(context, value);

        if (isValid()) {

            boolean result = false;

            try {
                URL url = new URI(getVerifyUrl()).toURL();
                URLConnection conn = url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                String postBody = createPostParameters(context, value);

                try (OutputStream out = conn.getOutputStream()) {
                    out.write(postBody.getBytes());
                    out.flush();
                }

                try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = rd.readLine()) != null) {
                        response.append(inputLine);
                    }

                    JSONObject json = new JSONObject(response.toString());
                    result = json.getBoolean("success");
                }
            }
            catch (Exception exception) {
                throw new FacesException(exception);
            }
            finally {
                // the captcha token is valid for only one request, in case of an ajax request we have to get a new one
                if (context.getPartialViewContext().isAjaxRequest()) {
                    String script = String.format("if (document.getElementById('%s-response')) {try {%s.reset();} catch (error) {PrimeFaces.error(error);}}",
                            getType(), getExecutor());
                    PrimeFaces.current().executeScript(script);
                }
            }

            if (!result) {
                setValid(false);

                String validatorMessage = getValidatorMessage();
                FacesMessage msg = null;

                if (validatorMessage != null) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, validatorMessage, validatorMessage);
                }
                else {
                    Object[] params = new Object[2];
                    params[0] = ComponentUtils.getLabel(context, this);
                    params[1] = value;

                    msg = MessageFactory.getFacesMessage(context, Captcha.INVALID_MESSAGE_ID, FacesMessage.SEVERITY_ERROR, params);
                }

                context.addMessage(getClientId(context), msg);
            }
        }
    }

    private String createPostParameters(FacesContext context, Object value) throws UnsupportedEncodingException {
        String privateKey = context.getExternalContext().getInitParameter(Captcha.PRIVATE_KEY);
        try {
            if (privateKey != null) {
                privateKey = context.getApplication().evaluateExpressionGet(context, privateKey, String.class);
            }
        }
        catch (ELException e) {
            LOGGER.fine(() -> "Error processing context parameter " + Captcha.PRIVATE_KEY + " as EL-expression: " + e.getMessage());
        }

        if (privateKey == null) {
            throw new FacesException("Cannot find private key for catpcha, use " + Captcha.PRIVATE_KEY + " context-param to define one");
        }

        StringBuilder postParams = new StringBuilder();
        postParams.append("secret=").append(URLEncoder.encode(privateKey, StandardCharsets.UTF_8));
        postParams.append("&response=").append(value == null ? Constants.EMPTY_STRING : URLEncoder.encode((String) value, StandardCharsets.UTF_8));

        String params = postParams.toString();
        postParams.setLength(0);

        return params;
    }
}