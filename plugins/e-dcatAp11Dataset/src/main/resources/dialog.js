define([], function () {
    function controller($scope, rdfService) {
        $scope.dialog = {} ;

        var rdf = rdfService.create('http://etl.linkedpipes.com/resource/components/e-dcatAp11Dataset/');

        var listToString = function(string) {
            return string.join();
        };

        var stringToList = function(list) {
            if (list === '') {
                return [];
            } else {
                var result = [];
                list.split(',').forEach(function (value) {
                    value = value.trim();
                    if (value !== '') {
                        result.push(value);
                    }
                });
                return result;
            }
        };

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            //Mandatory
            $scope.dialog.datasetIRI = rdf.getString(resource, 'datasetIRI');
            $scope.dialog.titles = rdf.getValue(resource, 'titles') ;
            $scope.dialog.descriptions = rdf.getValue(resource, 'descriptions') ;

            //Recommended
            $scope.dialog.contactPointTypeIRI = rdf.getString(resource, 'contactPointTypeIRI') ;
            $scope.dialog.contactPointName = rdf.getString(resource, 'contactPointName') ;
            $scope.dialog.contactPointEmail = rdf.getString(resource, 'contactPointEmail') ;
            $scope.dialog.keywords = rdf.getValue(resource, 'keywords') ;
            $scope.dialog.euThemeIRI = rdf.getString(resource, 'euThemeIRI') ;
            $scope.dialog.otherThemeIRIs = rdf.getValue(resource, 'otherThemeIRIs') ;

            $scope.dialog.publisherIRI = rdf.getString(resource, 'publisherIRI') ;
            $scope.dialog.publisherNames = rdf.getValue(resource, 'publisherNames') ;
            $scope.dialog.publisherTypeIRI = rdf.getString(resource, 'publisherTypeIRI') ;

            //Optional
            var languages = [];
            var inputLanguages = rdf.getValue(resource, 'languages');
            if (inputLanguages != undefined) {
                for (var index in inputLanguages) {
                    var item = inputLanguages[index];
                    languages.push({
                        'IRI' : item['@id'],
                        'value' : item['http://www.w3.org/2004/02/skos/core#prefLabel']
                    });
                }
            }
            $scope.dialog.languages = languages;

            $scope.dialog.accrualPeriodicityIRI = rdf.getString(resource, 'accrualPeriodicityIRI') ;
            $scope.dialog.issued = rdf.getDate(resource, 'issued') ;
            $scope.dialog.modified = rdf.getDate(resource, 'modified') ;
            $scope.dialog.spatialIRIs = rdf.getValue(resource, 'spatialIRIs') ;
            $scope.dialog.temporalStart = rdf.getDate(resource, 'temporalStart') ;
            $scope.dialog.temporalEnd = rdf.getDate(resource, 'temporalEnd') ;
            $scope.dialog.documentationIRIs = rdf.getValue(resource, 'documentationIRIs') ;
            $scope.dialog.accessRightsIRI = rdf.getString(resource, 'accessRightsIRI') ;
            $scope.dialog.identifier = rdf.getString(resource, 'identifier') ;
            $scope.dialog.datasetTypeIRI = rdf.getString(resource, 'datasetTypeIRI') ;
            $scope.dialog.provenances = rdf.getValue(resource, 'provenances') ;

            //Relations
            $scope.dialog.sampleIRIs = rdf.getValue(resource, 'sampleIRIs') ;
            $scope.dialog.landingPageIRIs = rdf.getValue(resource, 'landingPageIRIs') ;
            $scope.dialog.relatedIRIs = rdf.getValue(resource, 'relatedIRIs') ;
            $scope.dialog.confromsToIRIs = rdf.getValue(resource, 'confromsToIRIs') ;
            $scope.dialog.sourceIRIs = rdf.getValue(resource, 'sourceIRIs') ;
            $scope.dialog.hasVersionIRIs = rdf.getValue(resource, 'hasVersionIRIs') ;
            $scope.dialog.isVersionOfIRIs = rdf.getValue(resource, 'isVersionOfIRIs') ;

            //Versions
            $scope.dialog.version = rdf.getString(resource, 'version') ;
            $scope.dialog.versionNotes = rdf.getValue(resource, 'versionNotes') ;
        };

        $scope.getConfiguration = function () {
         	var resource = rdf.secureByType('Configuration');

            //Mandatory
            rdf.setString(resource, 'datasetIRI', $scope.dialog.datasetIRI);
            rdf.setValue(resource, 'titles', $scope.dialog.titles);
            rdf.setValue(resource, 'descriptions', $scope.dialog.descriptions);

            //Recommended
            rdf.setString(resource, 'contactPointTypeIRI', $scope.dialog.contactPointTypeIRI);
            rdf.setString(resource, 'contactPointName', $scope.dialog.contactPointName);
            rdf.setString(resource, 'contactPointEmail', $scope.dialog.contactPointEmail);

            rdf.setValue(resource, 'keywords', $scope.dialog.keywords);
            rdf.setString(resource, 'euThemeIRI', $scope.dialog.euThemeIRI);
            rdf.setValue(resource, 'otherThemeIRIs', $scope.dialog.otherThemeIRIs);

            rdf.setString(resource, 'publisherIRI', $scope.dialog.publisherIRI);
            rdf.setValue(resource, 'publisherNames', $scope.dialog.publisherNames);
            rdf.setString(resource, 'publisherTypeIRI', $scope.dialog.publisherTypeIRI);

            //Optional
            var languages = [];
            for (var index in $scope.dialog.languages) {
                var item = $scope.dialog.languages[index];
                languages.push({
                    '@id' : item['IRI'],
                    '@types': ['http://etl.linkedpipes.com/resource/components/e-dcatAp11Dataset/LanguageObject'],
                    'http://etl.linkedpipes.com/resource/components/e-dcatAp11Dataset/IRI' : item['IRI'],
                    'http://www.w3.org/2004/02/skos/core#prefLabel' : item['value']
                });
            }
            rdf.setValue(resource, 'languages', languages);

            rdf.setString(resource, 'accrualPeriodicityIRI', $scope.dialog.accrualPeriodicityIRI);
            rdf.setDate(resource, 'issued', $scope.dialog.issued);
            rdf.setDate(resource, 'modified', $scope.dialog.modified);
            rdf.setValue(resource, 'spatialIRIs', $scope.dialog.spatialIRIs);
            rdf.setDate(resource, 'temporalStart', $scope.dialog.temporalStart);
            rdf.setDate(resource, 'temporalEnd', $scope.dialog.temporalEnd);
            rdf.setValue(resource, 'documentationIRIs', $scope.dialog.documentationIRIs);
            rdf.setString(resource, 'accessRightsIRI', $scope.dialog.accessRightsIRI);
            rdf.setString(resource, 'identifier', $scope.dialog.identifier);
            rdf.setString(resource, 'datasetTypeIRI', $scope.dialog.datasetTypeIRI);
            rdf.setValue(resource, 'provenances', $scope.dialog.provenances);

            //Relations
            rdf.setValue(resource, 'sampleIRIs', $scope.dialog.sampleIRIs);
            rdf.setValue(resource, 'landingPageIRIs', $scope.dialog.landingPageIRIs);
            rdf.setValue(resource, 'relatedIRIs', $scope.dialog.relatedIRIs);
            rdf.setValue(resource, 'confromsToIRIs', $scope.dialog.confromsToIRIs);
            rdf.setValue(resource, 'sourceIRIs', $scope.dialog.sourceIRIs);
            rdf.setValue(resource, 'hasVersionIRIs', $scope.dialog.hasVersionIRIs);
            rdf.setValue(resource, 'isVersionOfIRIs', $scope.dialog.isVersionOfIRIs);

            //Versions
            rdf.setString(resource, 'version', $scope.dialog.version);
            rdf.setValue(resource, 'versionNotes', $scope.dialog.versionNotes);

            return rdf.getData();
        };


        $scope.languages =

            [
                {"IRI":"http://publications.europa.eu/resource/authority/language/AFR","value":"afrikaans"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/AMH","value":"amharic"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ARA","value":"arabic"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ARO","value":"araona"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/AYM","value":"aymara"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/AYO","value":"ayoreo"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/AZE","value":"azerbaijani"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/BEL","value":"belarusian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/BEN","value":"bengali"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/BIS","value":"bislama"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/BOD","value":"tibetan"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/BOS","value":"bosnian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/BRE","value":"breton"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/BRG","value":"baure"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/BUL","value":"bulgarian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CAL","value":"carolinian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CAO","value":"chacobo"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CAS","value":"chimane"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CAT","value":"catalan"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CAV","value":"cavineña"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CAW","value":"kallawaya"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CAX","value":"chiquitano"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CAZ","value":"canichana"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CES","value":"czech"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CHA","value":"chamorro"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CHN","value":"chinook jargon"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CKB","value":"central kurdish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CMN","value":"mandarin chinese"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/COR","value":"cornish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/COS","value":"corsican"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CRS","value":"seselwa creole french"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CSB","value":"kashubian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CYB","value":"cayubaba"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CYM","value":"welsh"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/DAN","value":"danish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/DEU","value":"german"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/DIV","value":"dhivehi"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/DSB","value":"lower sorbian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/DZO","value":"dzongkha"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ELL","value":"greek"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ENG","value":"english"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/EPO","value":"esperanto"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ESE","value":"ese ejja"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/EST","value":"estonian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/EUS","value":"basque"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/FAO","value":"faroese"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/FAS","value":"persian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/FIJ","value":"fijian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/FIL","value":"filipino"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/FIN","value":"finnish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/FRA","value":"french"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/FRY","value":"frisian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/FUR","value":"friulan"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/GLA","value":"scottish gaelic"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/GLE","value":"irish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/GLG","value":"galician"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/GLV","value":"manx"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/GRN","value":"guarani"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/GUG","value":"paraguayan guaraní"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/GYR","value":"guarayu"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/HAT","value":"haitian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/HBS","value":"serbo-croatian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/HCA","value":"andaman creole hindi"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/HEB","value":"hebrew"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/HIF","value":"fiji hindi"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/HIN","value":"hindi"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/HMO","value":"hiri motu"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/HRV","value":"croatian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/HSB","value":"upper sorbian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/HUN","value":"hungarian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/HYE","value":"armenian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/IGN","value":"ignaciano"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/INA","value":"interlingua"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/IND","value":"indonesian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ISL","value":"icelandic"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ITA","value":"italian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ITE","value":"itene"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ITO","value":"itonama"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/JPN","value":"japanese"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/KAL","value":"greenlandic"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/KAT","value":"georgian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/KAZ","value":"kazakh"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/KHM","value":"khmer"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/KIN","value":"kinyarwanda"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/KIR","value":"kyrgyz"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/KMR","value":"northern kurdish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/KON","value":"kikongo"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/KOR","value":"korean"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/KUR","value":"kurdish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/KXD","value":"brunei malay"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/LAO","value":"lao"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/LAT","value":"latin"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/LAV","value":"latvian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/LEC","value":"leco"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/LIN","value":"lingala"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/LIT","value":"lithuanian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/LTZ","value":"luxembourgish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/LUA","value":"luba-lulua"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MAH","value":"marshallese"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MIN","value":"minangkabau"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MIS","value":"uncoded languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MKD","value":"macedonian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MLG","value":"malagasy"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MLT","value":"maltese"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MON","value":"mongolian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MPD","value":"machinere"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MRI","value":"māori"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MTP","value":"wichí lhamtés nocten"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MUL","value":"multiple languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MYA","value":"burmese"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MZP","value":"movima"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/NAU","value":"nauruan"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/NBL","value":"southhern ndebele"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/NEP","value":"nepali"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/NIU","value":"niuean"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/NLD","value":"dutch"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/NNO","value":"norwegian nynorsk"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/NOB","value":"norwegian bokmål"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/NOR","value":"norwegian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/NSO","value":"northern sotho"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/NYA","value":"chewa"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/OCI","value":"occitan"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/OSS","value":"ossetic"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/PAP","value":"papiamento"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/PAU","value":"palauan"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/PCP","value":"pacahuara"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/PIH","value":"pitcairn-norfolk"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/POL","value":"polish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/POR","value":"portuguese"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/PRS","value":"dari"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/PSM","value":"pauserna"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/PUQ","value":"puquina"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/PUS","value":"pashto"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/QUE","value":"quechua"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/RAR","value":"rarotongan"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/REY","value":"reyesano"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ROH","value":"romansh"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ROM","value":"romani"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/RON","value":"romanian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/RUN","value":"kirundi"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/RUS","value":"russian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SAG","value":"sango"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SCO","value":"scots"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SDH","value":"southern kurdish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SIN","value":"sinhala"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SIP","value":"sikkimese"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SJE","value":"pite sami"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SLK","value":"slovak"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SLV","value":"slovenian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SME","value":"northern sami"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SMO","value":"samoan"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SOM","value":"somali"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SOT","value":"southern sotho"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SPA","value":"spanish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SQI","value":"albanian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SRP","value":"serbian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SRQ","value":"sirionó"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SSW","value":"swazi"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SWA","value":"swahili"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SWB","value":"comorian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SWE","value":"swedish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TAM","value":"tamil"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TET","value":"tetum"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TGK","value":"tajik"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/THA","value":"thai"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TIR","value":"tigrinya"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TKL","value":"tokelauan"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TNA","value":"tacana"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TNO","value":"toromono"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TON","value":"tongan"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TPI","value":"tok pisin"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TPJ","value":"tapieté"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TRN","value":"trinitario"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TSN","value":"tswana"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TSO","value":"tsonga"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TUK","value":"turkmen"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TUR","value":"turkish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TVL","value":"tuvaluan"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/UKR","value":"ukrainian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/UND","value":"undetermined"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/URD","value":"urdu"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/URE","value":"uru"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/UZB","value":"uzbek"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/VEN","value":"venda"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/VIE","value":"vietnamese"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/VLS","value":"flemish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/WLN","value":"walloon"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ZHO","value":"chinese"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/0D0","value":"valencian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/0E0","value":"montenegrin"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/AFA","value":"afro-asiatic languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ALG","value":"algonquian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/APA","value":"apache languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ART","value":"artificial languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ATH","value":"athapascan languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/AUS","value":"australian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/BAD","value":"banda languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/BAI","value":"bamileke languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/BAT","value":"baltic languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/BER","value":"berber languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/BNT","value":"bantu languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/BTK","value":"batak languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CAI","value":"central american indian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CAU","value":"caucasian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CEL","value":"celtic languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CMC","value":"chamic languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/CUS","value":"cushitic languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/DAY","value":"land dayak languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/DRA","value":"dravidian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/FIU","value":"finno-ugrian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/GEM","value":"germanic languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/IJO","value":"ijo languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/INC","value":"indic languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/INE","value":"indo-european languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/IRA","value":"iranian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/IRO","value":"iroquoian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/KAR","value":"karen languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/KHI","value":"khoisan languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/KRO","value":"kru languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MAP","value":"austronesian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MKH","value":"mon-khmer languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MNO","value":"manobo languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MOL","value":"moldavian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MSA","value":"malaysian"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MUN","value":"munda languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/MYN","value":"mayan languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/NAH","value":"nahuatl languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/NAI","value":"north american indian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/NIC","value":"niger-kordofanian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/NUB","value":"nubian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/OP_DATPRO","value":"provisional data"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/OTO","value":"otomian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/PAA","value":"papuan languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/PHI","value":"philippine languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/PRA","value":"prakrit languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ROA","value":"romance languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SAI","value":"south american indian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SAL","value":"salishan languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SEM","value":"semitic languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SGN","value":"sign languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SIO","value":"siouan languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SIT","value":"sino-tibetan languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SLA","value":"slavic languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SMI","value":"sami languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SON","value":"songhai languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/SSA","value":"nilo-saharan languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TAI","value":"tai languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TUP","value":"tupi languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/TUT","value":"altaic languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/WAK","value":"wakashan languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/WEN","value":"sorbian languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/XHO","value":"xhosa"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/YAA","value":"yaminava"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/YID","value":"yiddish"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/YPK","value":"yupik languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/YUE","value":"yue chinese"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/YUQ","value":"yuqui"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/YUZ","value":"yuracare"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ZLM","value":"malay"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ZND","value":"zande languages"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ZUL","value":"zulu"},
                {"IRI":"http://publications.europa.eu/resource/authority/language/ZXX","value":"no linguistic content"},
            ];

        $scope.publishertypes = [
            {
                "IRI":"http://purl.org/adms/publishertype/Academia-ScientificOrganisation",
                "label":"Academia/Scientific organisation"
            },
            {
                "IRI":"http://purl.org/adms/publishertype/Company",
                "label":"Company"
            },
            {
                "IRI":"http://purl.org/adms/publishertype/IndustryConsortium",
                "label":"Industry consortium"
            },
            {
                "IRI":"http://purl.org/adms/publishertype/LocalAuthority",
                "label":"Local Authority"
            },
            {
                "IRI":"http://purl.org/adms/publishertype/NationalAuthority",
                "label":"National authority"
            },
            {
                "IRI":"http://purl.org/adms/publishertype/NonGovernmentalOrganisation",
                "label":"Non-Governmental Organisation"
            },
            {
                "IRI":"http://purl.org/adms/publishertype/NonProfitOrganisation",
                "label":"Non-Profit Organisation"
            },
            {
                "IRI":"http://purl.org/adms/publishertype/PrivateIndividual(s)",
                "label":"Private Individual(s)"
            },
            {
                "IRI":"http://purl.org/adms/publishertype/RegionalAuthority",
                "label":"Regional authority"
            },
            {
                "IRI":"http://purl.org/adms/publishertype/StandardisationBody",
                "label":"Standardisation body"
            },
            {
                "IRI":"http://purl.org/adms/publishertype/SupraNationalAuthority",
                "label":"Supra-national authority"
            }
        ];

        $scope.frequencies = [ {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/ANNUAL",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "annual"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/ANNUAL_2",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "semiannual"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/ANNUAL_3",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "three times a year"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/BIENNIAL",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "biennial"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/BIMONTHLY",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "bimonthly"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/BIWEEKLY",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "biweekly"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/CONT",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "continuous"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/DAILY",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "daily"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/DAILY_2",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "twice a day"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/IRREG",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "irregular"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/MONTHLY",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "monthly"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/MONTHLY_2",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "semimonthly"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/MONTHLY_3",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "three times a month"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/NEVER",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "never"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/OP_DATPRO",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "Provisional data"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/OTHER",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "other"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/QUARTERLY",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "quarterly"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/TRIENNIAL",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "triennial"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/UNKNOWN",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "unknown"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/UPDATE_CONT",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "continuously updated"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/WEEKLY",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "weekly"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/WEEKLY_2",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "semiweekly"
            } ]
        }, {
            "@id" : "http://publications.europa.eu/resource/authority/frequency/WEEKLY_3",
            "http://www.w3.org/2004/02/skos/core#prefLabel" : [ {
                "@language" : "en",
                "@value" : "three times a week"
            } ]
        } ];

        $scope.euThemes = [
            {
                "@id": "http://publications.europa.eu/resource/authority/data-theme/AGRI",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Agriculture, fisheries, forestry and food"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/data-theme/ECON",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Economy and finance"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/data-theme/EDUC",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Education, culture and sport"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/data-theme/ENER",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Energy"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/data-theme/ENVI",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Environment"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/data-theme/GOVE",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Government and public sector"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/data-theme/HEAL",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Health"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/data-theme/INTR",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "International issues"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/data-theme/JUST",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Justice, legal system and public safety"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/data-theme/OP_DATPRO",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Provisional data"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/data-theme/REGI",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Regions and cities"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/data-theme/SOCI",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Population and society"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/data-theme/TECH",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Science and technology"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/data-theme/TRAN",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Transport"
                    }
                ]
            }
        ];

       $scope.accessRights = [
           {
               "@id": "http://publications.europa.eu/resource/authority/access-rights/PUBLIC",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Open data"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/access-rights/RESTRICTED",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Restricted"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/access-rights/NON-PUBLIC",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Non-public"
                   }
               ]
           }
       ];
       $scope.datasetTypes = [
           {
               "@id": "http://publications.europa.eu/resource/authority/dataset-type/CODE_LIST",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Code list"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/dataset-type/CORE_COMP",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Core component"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/dataset-type/DOMAIN_MODEL",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Domain model"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/dataset-type/DSCRP_SERV",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Service description"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/dataset-type/IEPD",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Information exchange package description"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/dataset-type/MAPPING",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Mapping"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/dataset-type/NAL",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Name authority list"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/dataset-type/ONTOLOGY",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Ontology"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/dataset-type/OP_DATPRO",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Provisional data"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/dataset-type/SCHEMA",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Schema"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/dataset-type/STATISTICAL",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Statistical"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/dataset-type/SYNTAX_ECD_SCHEME",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Syntax encoding scheme"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/dataset-type/TAXONOMY",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Taxonomy"
                   }
               ]
           },
           {
               "@id": "http://publications.europa.eu/resource/authority/dataset-type/THESAURUS",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Thesaurus"
                   }
               ]
           }
       ] ;

       $scope.createLangFilter = function createFilterFor(query) {
          var lowercaseQuery = angular.lowercase(query);
          return function filterFn(language) {
            return (language.value.indexOf(lowercaseQuery) !== -1);
        };
       };

      $scope.langSearch = function (query) {
          var results = query ? $scope.languages.filter( $scope.createLangFilter(query) ) : $scope.languages;
            return results;
        };

      $scope.transformChip = function(chip) {
          // If it is an object, it's already a known chip
          if (angular.isObject(chip)) {
            return chip;
          }
          // Otherwise, create a new one
          return { value: chip, IRI: 'new' }
      };

      $scope.dialog.languages = [] ;

    }
    //


    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});