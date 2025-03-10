/**
 * __PrimeFaces Captcha Widget__
 *
 * Captcha is a form validation component based on Recaptcha API V2.
 *
 * @typedef {"auto" | "light" | "dark"} PrimeFaces.widget.Captcha.Theme Captcha features light and dark modes for theme.
 *
 * @interface {PrimeFaces.widget.CaptchaCfg} cfg The configuration for the {@link  Captcha| Captcha widget}.
 * You can access this configuration via {@link PrimeFaces.widget.BaseWidget.cfg|BaseWidget.cfg}. Please note that this
 * configuration is usually meant to be read-only and should not be modified.
 * @extends {PrimeFaces.widget.BaseWidgetCfg} cfg
 *
 * @prop {string} cfg.callback Name of a function in the global scope. Callback to be executed when the user submits a
 * successful CAPTCHA response. The user's response, goggle recaptcha-response, will be the input for your callback
 * function.
 * @prop {string} cfg.expired Name of a function in the global scope. The callback executed when the captcha response
 * expires and the user needs to solve a new captcha.
 * @prop {string} cfg.language Language in which information about this captcha is shown.
 * @prop {string} cfg.sitekey Public recaptcha key for a specific domain (deprecated).
 * @prop {string} cfg.size Size of the recaptcha.
 * @prop {string} cfg.sourceUrl URL for the ReCaptcha JavaScript file. Some countries do not have access to Google.
 * @prop {number} cfg.tabindex Position of the input element in the tabbing order.
 * @prop {PrimeFaces.widget.Captcha.Theme} cfg.theme Theme of the captcha.
 */
PrimeFaces.widget.Captcha = class Captcha extends PrimeFaces.widget.BaseWidget {

    /**
     * @override
     * @inheritdoc
     * @param {TCfg} cfg
     */
    init(cfg) {
        super.init(cfg);
        this.cfg.language = this.cfg.language || 'en';
        this.cfg.theme = this.cfg.theme || PrimeFaces.env.getThemeContrast();

        var $this = this;

        window[this.getInitCallbackName()] = function() {
            $this.render();
        };

        this.appendScript();
    }

    /**
     * Appends the script to the document body.
     * @private
     */
    appendScript() {
        var nonce = '';
        if (PrimeFaces.csp.NONCE_VALUE) {
            nonce = 'nonce="' + PrimeFaces.csp.NONCE_VALUE + '"';
        }
        $(document.body).append('<script src="' + this.cfg.sourceUrl + '?onload=' + this.getInitCallbackName() + '&render=explicit&hl='
            + this.cfg.language + '" async defer ' + nonce + '>');
    }

    /**
     * Finds the name to use for the callback function set globally on the window.
     * @private
     * @return {string} Name for the global callback.
     */
    getInitCallbackName() {
        return this.cfg.widgetVar + '_initCallback';
    }

    /**
     * Renders the client-side parts of this widget.
     * @private
     */
    render() {
        var $this = this;
        var captchaExecutor = window[this.cfg.executor];
        captchaExecutor.render(this.jq.get(0), {
            'sitekey': this.cfg.sitekey,
            'tabindex': this.cfg.tabindex,
            'theme': this.cfg.theme,
            'callback': $this.cfg.callback,
            'expired-callback': $this.cfg.expired,
            'size': this.cfg.size
        });

        if (this.cfg.size === 'invisible') {
            captchaExecutor.execute();
        }

        window[this.getInitCallbackName()] = null;
    }

    /**
     * @override
     * @inheritdoc
     */
    destroy() {
        super.destroy();

        window[this.getInitCallbackName()] = null;
    }
}