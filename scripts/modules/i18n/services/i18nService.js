/*global WM, studio, window, wm*/
/*jslint todo: true */
/**
 * @ngdoc service
 * @name i18nService
 * @requires $rootScope
 * @requires $http
 * @requires Utils
 * @description
 * The `i18nService` service provides functionality required for localization & internationalization support.
 */

WM.module("i18n").service("i18nService", [
    "$rootScope",
    "Utils",
    "$http",
    "$locale",
    "CONSTANTS",
    "$q",

    function ($rootScope, Utils, $http, $locale, CONSTANTS, $q) {
        "use strict";

        var _supportedLocale = [],
            _defaultLocale,
            _appLocaleRootPath,
            _ngLocaleRootPath,
            _selectedLocale,
            localeKey = CONSTANTS.isStudioMode ? "locale" : "appLocale",
            _componentLocalePaths = [],
            _initSuccess = false,
            localeCodesMap = {};

        localeCodesMap = {
            "af": "Afrikaans",
            "af-na": "Afrikaans (Namibië)",
            "af-za": "Afrikaans (Suid-Afrika)",
            "am": "አማርኛ",
            "am-et": "አማርኛ (ኢትዮጵያ)", // Amharic (Ethiopia)
            "ar": "العربية", //Arabic
            "ar-001": "(العالم) العربية",// Arabic (world)
            "ar-ae": "(الإمارات العربية المتحدة) العربية ", // Arabic (United Arab Emirates)
            "ar-bh": "(البحرين) العربية‏", // Arabic (Bahrain)
            "ar-dz": "(الجزائر) العربية", // Arabic (Algeria)
            "ar-eg": "(مصر) العربية", // Arabic (Egypt)
            "ar-iq": "(العراق) العربية", // Arabic (Iraq)
            "ar-jo": "(الأردن) العربية", // Arabic (Jordan)
            "ar-kw": "(الكويت) العربية", // Arabic (Kuwait)
            "ar-lb": "(لبنان) العربية", // Arabic (Lebanon)
            "ar-ly": "(ليبيا) العربية", // Arabic (Libya)
            "ar-ma": "(المغرب) العربية", // Arabic (Morocco)
            "ar-om": "(عمان) العربية ", // Arabic (Oman)
            "ar-qa": "(قطر) العربية ", // Arabic (Qatar)
            "ar-sa": "(السعودية جزيره العرب) العربية", // Arabic (Saudi Arabia)
            "ar-sd": "(السودان) العربية", // Arabic (Sudan)
            "ar-sy": "(سوريا) العربية", // Arabic (Syria)
            "ar-tn": "(تونس) العربية", // Arabic (Tunisia)
            "ar-ye": "(اليمن) العربية", // Arabic (Yemen)
            "bg": "български", // Bulgarian
            "bg-bg": "български (България)", // Bulgarian (Bulgaria)
            "bn": "বাংলা", // Bengali
            "bn-bd": "বাংলা (বাংলাদেশ)", // Bengali (Bangladesh)
            "bn-in": "বাংলা (ভারত)", // Bengali (India)
            "ca": "català", // Catalan
            //"ca-ad": "", //
            "ca-es": "català (Espanya)", // Catalan (Spain)
            "cs": "čeština", // Czech
            "cs-cz": "čeština (Česká republika)", // Czech (Czech Republic)
            "da": "dansk", // Danish
            "da-dk": "dansk (Danmark)", // Danish (Denmark)
            "de": "Deutsch", // German
            "de-at": "Deutsch (Österreich)", // German (Austria)
            "de-be": "Deutsch (Belgien)", // German (Belgium)
            "de-ch": "Deutsch (Schweiz)", // German (Switzerland)
            "de-de": "Deutsch (Deutschland)", // German (Germany)
            "de-li": "Deutsch (Liechtenstein)", // German (Liechtenstein)
            "de-lu": "Deutsch (Luxemburg)", // German (Luxembourg)
            "el": "ελληνικά", // Greek
            "el-cy": "ελληνικά (Κύπρος)", // Greek (Cyprus)
            "el-gr": "ελληνικά (Ελλάδα)", // Greek (Greece)
            "en": "English", // English
            "en-as": "English (American Samoa)", // English (American Samoa)
            "en-au": "English (Australia)", // English (Australia)
            "en-bb": "English (Barbados)", // English (Barbados)
            "en-be": "English (Belgium)", // English (Belgium)
            "en-bm": "English (Bermuda)", // English (Bermuda)
            "en-bw": "English (Botswana)", // English (Botswana)
            "en-bz": "English (Belize)", // English (Belize)
            "en-ca": "English (Canada)", // English (Canada)
            //"en-dsrt":
            //"en-dsrt-us":
            //"en-fm":
            "en-gb": "English (United Kingdom)", // English (United Kingdom)
            "en-gu": "English (Guam)", // English (Guam)
            "en-gy": "English (Guyana)", // English (Guyana)
            "en-hk": "English (Hong Kong SAR China)", // English (Hong Kong SAR China)
            "en-ie": "English (Ireland)", // English (Ireland)
            "en-in": "English (India)", // English (India)
            //"en-iso":
            "en-jm": "English (Jamaica)", // English (Jamaica)
            "en-mh": "English (Marshall Islands)", // English (Marshall Islands)
            "en-mp": "English (Northern Mariana Islands)", // English (Northern Mariana Islands)
            "en-mt": "English (Malta)", // English (Malta)
            "en-mu": "English (Mauritius)", // English (Mauritius)
            "en-na": "English (Namibia)", // English (Namibia)
            "en-nz": "English (New Zealand)", // English (New Zealand)
            "en-ph": "English (Philippines)", // English (Philippines)
            "en-pk": "English (Pakistan)", // English (Pakistan)
            //"en-pr":
            //"en-pw":
            "en-sg": "English (Singapore)", // English (Singapore)
            //"en-tc":
            "en-tt": "English (Trinidad and Tobago)", // English (Trinidad and Tobago)
            "en-um": "English (U.S. Minor Outlying Islands)", // English (U.S. Minor Outlying Islands)
            "en-us": "English (United States)", // English (United States)
            //"en-vg":
            "en-vi": "English (U.S. Virgin Islands)", // English (U.S. Virgin Islands)
            "en-za": "English (South Africa)", // English (South Africa)
            "en-zw": "English (Zimbabwe)", // English (Zimbabwe)
            "es": "español", // Spanish
            "es-419": "español (Latinoamérica)", // Spanish (Latin America)
            "es-ar": "español (Argentina)", // Spanish (Argentina)
            "es-bo": "español (Bolivia)", // Spanish (Bolivia)
            "es-cl": "español (Chile)", // Spanish (Chile)
            "es-co": "español (colombia)", // Spanish (Colombia)
            "es-cr": "español (Costa Rica)", // Spanish (Costa Rica)
            "es-do": "español (República Dominicana)", // Spanish (Dominican Republic)
            //"es-ea":
            "es-ec": "español (Ecuador)", // Spanish (Ecuador)
            "es-es": "español (España)", // Spanish (Spain)
            "es-gq": "española (Guinea Ecuatorial)", // Spanish (Equatorial Guinea)
            "es-gt": "español (Guatemala)", // Spanish (Guatemala)
            "es-hn": "español (Honduras)", // Spanish (Honduras)
            //"es-ic": "", //
            "es-mx": "español (México)", // Spanish (Mexico)
            "es-ni": "español (Nicaragua)", // Spanish (Nicaragua)
            "es-pa": "español (Panamá)", // Spanish (Panama)
            "es-pe": "español (Perú)", // Spanish (Peru)
            "es-pr": "español (Puerto Rico)", // Spanish (Puerto Rico)
            "es-py": "español (Paraguay)", // Spanish (Paraguay)
            "es-sv": "español (El Salvador)", // Spanish (El Salvador)
            "es-us": "español (estados unidos)", // Spanish (United States)
            "es-uy": "español (Uruguay)", // Spanish (Uruguay)
            "es-ve": "español (Venezuela)", // Spanish (Venezuela)
            "et": "eesti", // Estonian
            "et-ee": "eesti (Eesti)", // Estonian (Estonia)
            "eu": "euskara", // Basque
            "eu-es": "euskara (euskara)", // Basque (Spain)
            "fa": "فارسی", // Persian
            "fa-af": "(افغانستان) فارسی", // Persian (Afghanistan)
            "fa-ir": "(ایران) فارسی", // Persian (Iran)
            "fi": "suomi", // Finnish
            "fi-fi": "suomi (Suomi)", // Finnish (Finland)
            "fil": "Filipino", // Filipino
            "fil-ph": "Filipino (Pilipinas)", // Filipino (Pilipinas
            "fr": "français", // French
            "fr-be": "français (belgique)", // French (Belgium)
            "fr-bf": "français (Burkina Faso)", // French (Burkina Faso)
            "fr-bi": "français (Burundi)", // French (Burundi)
            "fr-bj": "français (Bénin)", // French (Benin)
            "fr-bl": "français (saints Barthélemy)", // French (Saint Barthélemy)
            "fr-ca": "français (Canada)", // French (Canada)
            "fr-cd": "français (Congo-Kinshasa)", // French (Congo - Kinshasa)
            "fr-cf": "français (République centrafricaine)", // French (Central African Republic)
            "fr-cg": "français (Congo-Brazzaville)", // French (Congo - Brazzaville)
            "fr-ch": "français (Suisse)", // French (Switzerland)
            "fr-ci": "français (Côte d’Ivoire)", // French (Côte d’Ivoire)
            "fr-cm": "français (Cameroun)", // French (Cameroon)
            "fr-dj": "français (Djibouti)", // French (Djibouti)
            "fr-fr": "français (France)", // French (France)
            "fr-ga": "français (Gabon)", // French (Gabon)
            "fr-gf": "français (Guyane française)", // French (French Guiana)
            "fr-gn": "français (Guinée)", // French (Guinea)
            "fr-gp": "français (Guadeloupe)", // French (Guadeloupe)
            "fr-gq": "français (Guinée équatoriale)", // French (Equatorial Guinea)
            "fr-km": "français (Comores)", // French (Comoros)
            "fr-lu": "français (luxembourg)", // French (Luxembourg)
            "fr-mc": "français (Monaco)", // French (Monaco)
            "fr-mf": "français (Saint-Martin)", // French (Saint Martin)
            "fr-mg": "français (Madagascar)", // French (Madagascar)
            "fr-ml": "français (Mali)", // French (Mali)
            "fr-mq": "français (Martinique)", // French (Martinique)
            "fr-ne": "français (Niger)", // French (Niger)
            "fr-re": "français (Réunion)", // French (Réunion)
            "fr-yt": "français (Mayotte)", // French (Mayotte)
            "gl": "galego", // Galician
            "gl-es": "galego (España)", // Galician (Spain)
            "gsw": "Elsässisch", // Alsatian
            //"gsw-ch": "", //
            "gu": "ગુજરાતી", // Gujarati
            "gu-in": "ગુજરાતી (ભારત)", // Gujarati (India)
            "he": "עברית", // Hebrew
            "he-il": "(ישראל) עברית", // Hebrew (Israel)
            "hi": "हिंदी", // Hindi
            "hi-in": "हिंदी (भारत)", // Hindi (India)
            "hr": "hrvatski", // Croatian
            "hr-hr": "hrvatski (Hrvatska)", // Croatian (Croatia)
            "hu": "magyar", // Hungarian
            "hu-hu": "magyar (Magyarország)", // Hungarian (Hungary)
            "id": "indonesia", // Indonesian
            "id-id": "indonesia (Indonesia)", // Indonesian (Indonesia)
            //"in": "", //
            "is": "íslenska", // Icelandic
            "is-is": "íslenska (Ísland)", // Icelandic (Iceland)
            "it": "italiano", // Italian
            "it-it": "italiano (svizzera)", //Italian (Switzerland)
            //"it-sm": "", //
            //"iw": "", //
            "ja": "日本の", // Japanese
            "ja-jp": "日本語（日本）", // Japanese (Japan)
            "kn": "ಕನ್ನಡ", // Kannada
            "kn-in": "ಕನ್ನಡ (ಭಾರತ)", // Kannada (India)
            "ko": "한국의", // Korean
            "ko-kr": "한국어 (한국)", // Korean (South Korea)
            "ln": "Lingala", // Lingala
            "ln-cd": "Lingala (Congo - Kinshasa)", //Lingala (Congo - Kinshasa)
            "lt": "lietuvių", // Lithuanian
            "lt-lt": "lietuvių (Lietuva)", // Lithuanian (Lithuania)
            "lv": "latviešu", // Latvian
            "lv-lv": "latviešu (Latvija)", // Latvian (Latvia)
            "ml": "മലയാളം", // Malayalam
            "ml-in": "മലയാളം (ഭാരതം)", // Malayalam (India)
            "mr": "मराठी", // Marathi
            "mr-in": "मराठी (भारत)", // Marathi (India)
            "ms": "Melayu", // Malay
            "ms-my": "Melayu (Malaysia)", // Malay (Malaysia)
            "mt": "Malti", // Maltese
            "mt-mt": "Malti (Malta)", // Maltese (Malta)
            "nl": "nederlands", // Dutch
            "nl-cw": "nederlands (Curacao)", // Dutch (Curaçao)
            "nl-nl": "nederlands (Nederland)", // Dutch (Netherlands)
            "nl-sx": "nederlands (Sint Maarten)", // Dutch (Sint Maarten)
            "no": "norsk", // Norwegian
            "or": "ଓଡ଼ିଆ", // Oriya
            "or-in": "ଓଡ଼ିଆ (ଭାରତ)", // Oriya (India)
            "pl": "polski", // Polish
            "pl-pl": "polski (Polska)", // Polish (Poland)
            "pt": "Português", // Portuguese
            "pt-br": "Português (Brasil)", // Portuguese (Brazil)
            "pt-pt": "português (Portugal)", // Portuguese (Portugal)
            "pt-st": "português (São Tomé and Príncipe)", // Portuguese (São Tomé and Príncipe)
            //"pt-tl": "", //
            "ro": "română", // Romanian
            "ro-ro": "română (România)", // Romanian (Romania)
            "ru": "русский", // Russian
            "ru-ru": "русский (Россия)", // Russian (Russia)
            "sk": "slovenčina", // Slovak
            "sk-sk": "Slovak (Slovakia)", // slovenčina (Slovenská republika)
            "sl": "slovenski", // Slovenian
            "sl-si": "slovenski (Slovenija)", // Slovenian (Slovenia)
            "sq": "shqipe", // Albanian
            "sq-al": "shqipe (Shqipëria)", // Albanian (Albania)
            "sr": "srpski", // Serbian
            "sr-cyrl-rs": "српски (Србија)", // Serbian (Cyrillic, Serbia)
            "sr-latn-rs": "srpski (Srbija)", // Serbian (Latin, Serbia)
            "sv": "svenska", // Swedish
            "sv-se": "svenska (Sverige)", // Swedish (Sweden)
            "sw": "Swahili", //Swahili
            "sw-tz": "Swahili (Tanzania)", // Swahili (Tanzania)
            "ta": "தமிழ்", // Tamil
            "ta-in": "தமிழ் (இந்தியா)", // Tamil (India)
            "te": "తెలుగు", // Telugu
            "te-in": "తెలుగు (భారత దేశం)", // Telugu (India)
            "th": "ไทย", // Thai
            "th-th": "ไทย (ไทย)", // Thai (Thailand)
            "tl": "Tigrinya ", // Tigrinya
            "tr": "Türkçe", // Turkish
            "tr-tr": "Türkçe (Türkiye)", // Turkish (Turkey)
            "uk": "українська", // Ukrainian
            "uk-ua": "українська (Україна)", // Ukrainian (Ukraine)
            "ur": "اُردو‏", // Urdu‎
            "ur-pk": " (پاکستان)‏ اُردو", // Urdu (Islamic Republic of Pakistan)‎
            "vi": "Tiếng Việt", // Vietnamese
            "vi-vn": "Tiếng Việt (Việt Nam)", // Vietnamese (Vietnam)
            "zh": "中文", // Chinese
            "zh-cn": "中文(中华人民共和国)", // Chinese (Simplified, PRC)
            "zh-hans-cn": "中国（简体，中国）", //Chinese (Simplified, China)
            "zh-hk": "中文(香港特別行政區)", // Chinese (Traditional, Hong Kong S.A.R.)
            "zh-tw": "中文(台灣)", // Chinese (Traditional, Taiwan)
            "zu": "isiZulu", // Zulu
            "zu-za": "isiZulu (iNingizimu Afrika)" // Zulu (South Africa)
        };

        $rootScope[localeKey] = {}; /* reset the locale object on the rootScope */

        // function to get the parameterized localized messages.
        $rootScope.getLocalizedMessage = function () {
            var args = Array.prototype.slice.call(arguments),
                key = $rootScope[localeKey][args.shift()];

            return Utils.replace(key, args);
        };

        /* helper function VERY SPECIFIC to extend $locale */
        function extendLocale(dst, src) {
            dst.id = src.id;
            dst.pluralCat = src.pluralCat;
            ["DATETIME_FORMATS", "NUMBER_FORMATS"].forEach(function (type) {
                Object.keys(src[type]).forEach(function (key) {
                    dst[type][key] = src[type][key];
                });
            });
        }

        function loadComponentLocaleBundles() {
            var deferred = $q.defer(),
                count = _componentLocalePaths.length,
                content = {};

            if (!count) {
                deferred.resolve(content);
            }

            _componentLocalePaths.forEach(function (path) {
                path  += _selectedLocale + ".json";
                $http
                    .get(path)
                    .success(function (response) {
                        /* extend the $rootScope.locale object with the response json */
                        WM.extend(content, response);
                        count--;
                        if (!count) {
                            deferred.resolve(content);
                        }
                    });
            });

            return deferred.promise;
        }

        /**
         * Loads the localeBundle.
         * Placeholders in the path will be replaced with the selected locale value and localeBundle will be loaded using the constructed path.
         */
        function loadAppLocaleBundle(content) {
            var path = _appLocaleRootPath + _selectedLocale + ".json";
            /* load the localeBundle */

            $http
                .get(path)
                .success(function (response) {
                    /* extend the $rootScope.locale object with the response json */
                    WM.extend($rootScope[localeKey], response, content);
                });
        }

        /**
         * Loads the angular ngLocale resource of the selected locale.
         */
        function loadNgLocaleBundle() {
            var deferred = $q.defer(),
                ngLocaleFilePath = _ngLocaleRootPath;

            if (!ngLocaleFilePath) {
                deferred.resolve();
            }

            ngLocaleFilePath += "angular-locale_" + _selectedLocale + ".js";

            /* load ngLocale module for the selected locale */

            /* load the script tag */
            WM.element.ajax({
                dataType: "script",
                url: ngLocaleFilePath,
                cache: true /* read the script tag from the cache when available */
            }).done(function () {
                extendLocale($locale, WM.injector(["ngLocale"]).get("$locale"));
                deferred.resolve();
            });
            return deferred.promise;
        }

        /* loads the locale bundles */
        function loadLocaleBundles() {
            loadNgLocaleBundle()
                .then(loadComponentLocaleBundles)
                .then(loadAppLocaleBundle);
        }

        /*
         * this method will be triggered when there is a change in the locale selection.
         * load the localeBundles related to the selected locale.
         * This method emits "locale-changed" event on the $rootScope.
         */
        function setSelectedLocale(locale) {
            if (!_initSuccess) {
                return;
            }

            if (_supportedLocale.indexOf(locale) === -1) {
                return;
            }

            if (!locale || locale === _selectedLocale) {
                return;
            }

            _selectedLocale = locale;

            /* reset the $rootScope.locale object */
            $rootScope[localeKey] = {};

            /* load the locale bundles of the selected locale */
            loadLocaleBundles();
        }

        /* returns the default locale */
        function getDefaultLocale() {
            return _defaultLocale;
        }

        /* returns the selected locale */
        function getSelectedLocale() {
            return _selectedLocale;
        }

        function init(supportedLocale, defaultLocale, appLocaleRootPath, ngLocaleRootPath) {
            /* supportedLocale must be a non empty array */
            if (!supportedLocale || !WM.isArray(supportedLocale) || supportedLocale.length === 0) {
                return;
            }

            _supportedLocale = supportedLocale;

            /* appLocaleRootPath must be a non empty string */
            if (!appLocaleRootPath || appLocaleRootPath.length === 0) {
                return;
            }
            _appLocaleRootPath = appLocaleRootPath;
            _defaultLocale = defaultLocale || supportedLocale[0];
            _ngLocaleRootPath = ngLocaleRootPath;
            _initSuccess = true;

            /* load the default locale bundle */
            setSelectedLocale(_defaultLocale);
        }

        function extendAppLocale(path) {
            path  += _selectedLocale + ".json";
            $http
                .get(path)
                .success(function (response) {
                    /* extend the $rootScope.locale object with the response json */
                    WM.extend($rootScope.appLocale, response);
                });
        }

        // loads the locale bundle related to a component
        function loadComponentLocaleBundle(path) {
            if (!_initSuccess) {
                return;
            }
            if (path && _componentLocalePaths.indexOf(path) === -1) {
                _componentLocalePaths.push(path);
                extendAppLocale(path);
            }
        }

        function getLocaleCodesMap() {
            return localeCodesMap;
        }

        return {
            /**
             * @ngdoc function
             * @name i18nService#init
             * @methodOf i18nService
             * @function
             *
             * @description
             * initialize the i18nService.
             * load the default locale bundle
             *
             * @param {Array} supportedLocale array of supported locale
             * @param {String} defaultLocale default locale
             * @param {String} appLocaleRootPath root path of the application related locale files
             * @param {String} appLocaleRootPath root path of the ngLocale files
             */
            init: init,

            /**
             * @ngdoc function
             * @name i18nService#getDefaultLocale
             * @methodOf i18nService
             * @function
             *
             * @description
             * returns the default locale.
             */
            getDefaultLocale: getDefaultLocale,

            /**
             * @ngdoc function
             * @name i18nService#getSelectedLocale
             * @methodOf i18nService
             * @function
             *
             * @description
             * returns the locale for the the locale bundle is recently loaded.
             */
            getSelectedLocale: getSelectedLocale,

            /**
             * @ngdoc function
             * @name i18nService#setSelectedLocale
             * @methodOf i18nService
             * @function
             *
             * @description
             * sets the localeid passed as selected and load the locale bundle related to it.
             *
             * @param {String} locale locale id for which the bundle needs to be loaded.
             */
            setSelectedLocale: setSelectedLocale,

            /**
             * @ngdoc function
             * @name i18nService#loadComponentLocaleBundle
             * @methodOf i18nService
             * @function
             *
             * @description
             * load the locale bundle of a component
             *
             * @param {String} path path from where the component locale bundle needs to be loaded
             */
            loadComponentLocaleBundle: loadComponentLocaleBundle,

            /**
             * @ngdoc function
             * @name i18nService#getLocaleCodesMap
             * @methodOf i18nService
             * @function
             *
             * @description
             * returns the available locale codes - name map
             */
            getLocaleCodesMap: getLocaleCodesMap
        };
    }
]);