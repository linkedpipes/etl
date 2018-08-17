((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {

    const defaults = {
        "lp-landing": "/executions",
        "lp-initial-list-size": "15"
    };

    const cookiesOptions = {
        "expires": getExpiration()
    };

    function getExpiration() {
        const expires = new Date();
        expires.setYear(expires.getFullYear() + 2);
        return expires;
    }

    function service($cookies) {

        this.getListSize = () => {
            return parseInt(getCookiesValue("lp-initial-list-size"));
        };

        this.setListSize = (value) => {
            if (isNaN(value)) {
                $cookies.remove("lp-initial-list-size");
            } else {
                $cookies.put("lp-initial-list-size", value, cookiesOptions);
            }
        };

        function getCookiesValue(name) {
            const value = $cookies.get(name);
            if (value === undefined) {
                return defaults[name];
            } else {
                return value;
            }
        }

        this.getLandingPage = () => {
            return getCookiesValue("lp-landing");
        };

        this.setLandingPage = (value) => {
            $cookies.put("lp-landing", value, cookiesOptions);
        };

    }

    service.$inject = ["$cookies"];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        app.service("personalization", service);
    }

});