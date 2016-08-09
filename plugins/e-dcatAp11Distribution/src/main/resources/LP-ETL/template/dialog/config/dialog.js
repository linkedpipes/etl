define([], function () {
    function controller($scope, $service, rdfService) {

        var rdf = rdfService.create('http://etl.linkedpipes.com/resource/components/e-dcatAp11Distribution/');

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

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            //Mandatory
            $scope.dialog.getDatasetIRIFromInput = rdf.getBoolean(resource, 'getDatasetIRIFromInput') ;
            $scope.dialog.datasetIRI = rdf.getString(resource, 'datasetIRI') ;
            $scope.dialog.distributionIRI = rdf.getString(resource, 'distributionIRI') ;
            $scope.dialog.genDistroIRI = rdf.getBoolean(resource, 'genDistroIRI') ;
            $scope.dialog.accessURLs = rdf.getValue(resource, 'accessURLs') ;

            //Recommended
            $scope.dialog.formatIRI = rdf.getString(resource, 'formatIRI') ;
            $scope.dialog.licenseIRI = rdf.getString(resource, 'licenseIRI') ;
            $scope.dialog.licenseTypeIRI = rdf.getString(resource, 'licenseTypeIRI') ;
            $scope.dialog.descriptions = rdf.getValue(resource, 'descriptions') ;

            //Download
            $scope.dialog.downloadURLs = rdf.getValue(resource, 'downloadURLs') ;
            $scope.dialog.mediaType = rdf.getString(resource, 'mediaType') ;

            //Documentation
            $scope.dialog.titles = rdf.getValue(resource, 'titles') ;
            $scope.dialog.documentationIRIs = rdf.getValue(resource, 'documentationIRIs') ;
            $scope.dialog.languagesFromDataset = rdf.getBoolean(resource, 'languagesFromDataset') ;

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
            $scope.dialog.conformsToIRIs = rdf.getValue(resource, 'conformsToIRIs') ;
            $scope.dialog.statusIRI = rdf.getString(resource, 'statusIRI') ;
            $scope.dialog.issuedFromDataset = rdf.getBoolean(resource, 'issuedFromDataset') ;
            $scope.dialog.issued = rdf.getDate(resource, 'issued') ;
            $scope.dialog.modifiedFromDataset = rdf.getBoolean(resource, 'modifiedFromDataset') ;
            $scope.dialog.modifiedNow = rdf.getBoolean(resource, 'modifiedNow') ;
            $scope.dialog.modified = rdf.getDate(resource, 'modified') ;
            $scope.dialog.rightsIRI = rdf.getString(resource, 'rightsIRI') ;

            //Verification
            $scope.dialog.byteSize = rdf.getInteger(resource, 'byteSize') ;
            $scope.dialog.checksum = rdf.getString(resource, 'checksum') ;

            //Series
            $scope.dialog.spatialIRIs = rdf.getValue(resource, 'spatialIRIs') ;
            $scope.dialog.temporalStart = rdf.getDate(resource, 'temporalStart') ;
            $scope.dialog.temporalEnd = rdf.getDate(resource, 'temporalEnd') ;

            //StatDCAT-AP draft 4
            $scope.dialog.distributionTypeIRI = rdf.getValue(resource, 'distributionTypeIRI') ;

        };

        function saveDialog() {
         	var resource = rdf.secureByType('Configuration');

            //Mandatory
            rdf.setString(resource, 'datasetIRI', $scope.dialog.datasetIRI);
            rdf.setBoolean(resource, 'getDatasetIRIFromInput', $scope.dialog.getDatasetIRIFromInput);
            rdf.setString(resource, 'distributionIRI', $scope.dialog.distributionIRI);
            rdf.setBoolean(resource, 'genDistroIRI', $scope.dialog.genDistroIRI);
            rdf.setValue(resource, 'accessURLs', $scope.dialog.accessURLs);

            //Recommended
            rdf.setString(resource, 'formatIRI', $scope.dialog.formatIRI);
            rdf.setString(resource, 'licenseIRI', $scope.dialog.licenseIRI);
            rdf.setString(resource, 'licenseTypeIRI', $scope.dialog.licenseTypeIRI);
            rdf.setValue(resource, 'descriptions', $scope.dialog.descriptions);

            //Download
            rdf.setValue(resource, 'downloadURLs', $scope.dialog.downloadURLs);
            rdf.setString(resource, 'mediaType', $scope.dialog.mediaType);

            //Documentation
            rdf.setValue(resource, 'titles', $scope.dialog.titles);
            rdf.setValue(resource, 'documentationIRIs', $scope.dialog.documentationIRIs);
            rdf.setBoolean(resource, 'languagesFromDataset', $scope.dialog.languagesFromDataset);

            var languages = [];
            for (var index in $scope.dialog.languages) {
                var item = $scope.dialog.languages[index];
                languages.push({
                    '@id' : item['IRI'],
                    '@types': ['http://etl.linkedpipes.com/resource/components/e-dcatAp11Distribution/LanguageObject'],
                    'http://etl.linkedpipes.com/resource/components/e-dcatAp11Dataset/IRI' : item['IRI'],
                    'http://www.w3.org/2004/02/skos/core#prefLabel' : item['value']
                });
            }
            rdf.setValue(resource, 'languages', languages);

            rdf.setValue(resource, 'conformsToIRIs', $scope.dialog.conformsToIRIs);
            rdf.setValue(resource, 'statusIRI', $scope.dialog.statusIRI);
            rdf.setBoolean(resource, 'issuedFromDataset', $scope.dialog.issuedFromDataset);
            rdf.setDate(resource, 'issued', $scope.dialog.issued);
            rdf.setBoolean(resource, 'modifiedFromDataset', $scope.dialog.modifiedFromDataset);
            rdf.setBoolean(resource, 'modifiedNow', $scope.dialog.modifiedNow);
            rdf.setDate(resource, 'modified', $scope.dialog.modified);
            rdf.setString(resource, 'rightsIRI', $scope.dialog.rightsIRI);

            //Verification
            rdf.setInteger(resource, 'byteSize', $scope.dialog.byteSize);
            rdf.setString(resource, 'checksum', $scope.dialog.checksum);

            //Series
            rdf.setValue(resource, 'spatialIRIs', $scope.dialog.spatialIRIs);
            rdf.setDate(resource, 'temporalStart', $scope.dialog.temporalStart);
            rdf.setDate(resource, 'temporalEnd', $scope.dialog.temporalEnd);

            //StatDCAT-AP draft 4
            rdf.setValue(resource, 'distributionTypeIRI', $scope.dialog.distributionTypeIRI);

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

        $scope.formats = [
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/ARC",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "ARC"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/ATOM",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Atom Feed"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/AZW",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Amazon Kindle eBook"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/BIN",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Binary Data"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/BMP",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Bitmap Image File"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/CSS",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "CSS"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/CSV",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "CSV"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/DBF",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "DBF"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/DCR",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "DCR File"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/DMP",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Oracle Dump"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/DOC",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Word DOC"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/DOCX",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Word DOCX"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/DTD_SGML",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "SGML DTD"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/DTD_XML",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "XML DTD"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/E00",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "E00"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/ECW",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "ECW"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/EPS",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Encapsulated Postscript"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/EPUB",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "EPUB"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/FMX2",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Formex 2"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/FMX3",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Formex 3"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/FMX4",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Formex 4"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/GDB",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Esri File Geodatabase"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/GIF",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "GIF"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/GML",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "GML"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/GMZ",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Zipped GML"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/GRID_ASCII",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Esri ASCII grid"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/GZIP",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "GNU zip"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/HDF",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "HDF"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/HTML",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "HTML"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/HTML_SIMPL",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "HTML simplified"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/INDD",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "INDD"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/JPEG",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "JPEG"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/JPEG2000",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "JPEG 2000"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/JSON",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "JSON"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/JSON_LD",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "JSON-LD"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/KML",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "KML"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/KMZ",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "KMZ"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/LAS",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "LASer file"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/LAZ",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Compressed LAS file"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/MAP_PRVW",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "ArcGIS Map Preview"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/MAP_SRVC",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "ArcGIS Map Service"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/MDB",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "MDB"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/METS",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "METS XML"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/METS_ZIP",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "METS package"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/MOBI",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Mobipocket eBook"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/MOP",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "MOP"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/MSG_HTTP",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "HTTP Message"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/MXD",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "MXD"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/NETCDF",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "NetCDF"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/OCTET",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Octet Stream"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/ODB",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "OpenDocument Database"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/ODC",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "OpenDocument Chart"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/ODF",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "ODF"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/ODG",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "OpenDocument Image"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/ODS",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "ODS"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/ODT",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "ODT"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/OP_DATPRO",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Provisional data"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/OVF",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "OVF"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/OWL",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "OWL"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/PDF",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "PDF"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/PDF1X",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "PDF1X"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/PDFA1A",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "PDF/A-1a"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/PDFA1B",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "PDF/A-1b"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/PDFX",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "PDF/X"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/PNG",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "PNG"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/PPS",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "PowerPoint Slide Show"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/PPSX",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "PowerPoint PPSX"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/PPT",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "PowerPoint PPT"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/PPTX",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "PowerPoint PPTX"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/PS",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "PS"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/PSD",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "PSD"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/RDF",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "RDF"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/RDFA",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "RDFa"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/RDF_N_QUADS",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "RDF N-Quads"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/RDF_N_TRIPLES",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "RDF N-Triples"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/RDF_TRIG",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "RDF TriG"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/RDF_TURTLE",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "RDF Turtle"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/RDF_XML",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "RDF XML"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/REST",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Esri REST"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/RSS",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "RSS feed"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/RTF",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "RTF"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/SCHEMA_XML",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "XML schema"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/SDMX",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "SDMX"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/SGML",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "SGML"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/SHP",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Esri Shape"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/SKOS_XML",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "SKOS"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/SPARQLQ",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "SPARQL"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/SPARQLQRES",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "SPARQL results"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/TAB",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "MapInfo TAB file"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/TAB_RSTR",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "MapInfo TAB raster file"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/TAR",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "TAR"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/TAR_GZ",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "TAR GZ"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/TAR_XZ",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "TAR XZ"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/TIFF",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "TIFF"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/TIFF_FX",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "TIFF FX"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/TMX",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "TMX"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/TSV",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "TSV"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/TXT",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Plain text"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/WARC_GZ",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "WARC GZ"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/WORLD",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "World file"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/XHTML",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "XHTML"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/XHTML_SIMPL",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "XHTML simplified"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/XLIFF",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "XLIFF"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/XLS",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Excel XLS"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/XLSX",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Excel XLSX"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/XML",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "XML"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/XSLFO",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "XSL-FO"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/XSLT",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "XSLT"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/XYZ",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "XYZ Chemical File"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/file-type/ZIP",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "ZIP"
                    }
                ]
            }
        ];
       $scope.states = [
           {
               "@id": "http://purl.org/adms/status/Completed",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Completed"
                   }
               ]
           },
           {
               "@id": "http://purl.org/adms/status/Deprecated",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Deprecated"
                   }
               ]
           },
           {
               "@id": "http://purl.org/adms/status/UnderDevelopment",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Under development"
                   }
               ]
           },
           {
               "@id": "http://purl.org/adms/status/Withdrawn",
               "http://www.w3.org/2004/02/skos/core#prefLabel": [
                   {
                       "@language": "en",
                       "@value": "Withdrawn"
                   }
               ]
           }
       ];
        $scope.licenseTypes = [
            {
                "@id": "http://purl.org/adms/licencetype/Attribution",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Attribution"
                    }
                ]
            },
            {
                "@id": "http://purl.org/adms/licencetype/PublicDomain",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Public domain"
                    }
                ]
            },
            {
                "@id": "http://purl.org/adms/licencetype/ViralEffect-ShareAlike",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Viral effect (a.k.a. Share-alike)"
                    }
                ]
            },
            {
                "@id": "http://purl.org/adms/licencetype/ShareAlike-NotCompatible",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Share-alike / copyleft - not compatible/interoperable with other copyleft licences"
                    }
                ]
            },
            {
                "@id": "http://purl.org/adms/licencetype/ShareAlike-Compatible",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Share-alike / copyleft on source code or with compatibility exceptions for larger work and interoperability"
                    }
                ]
            },
            {
                "@id": "http://purl.org/adms/licencetype/NonCommercialUseOnly",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Non-commercial use only"
                    }
                ]
            },
            {
                "@id": "http://purl.org/adms/licencetype/NoDerivativeWork",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "No derivative work"
                    }
                ]
            },
            {
                "@id": "http://purl.org/adms/licencetype/RoyaltiesRequired",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Royalties required"
                    }
                ]
            },
            {
                "@id": "http://purl.org/adms/licencetype/ReservedNames-Endorsement-OfficialStatus",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Reserved names / endorsement / official status"
                    }
                ]
            },
            {
                "@id": "http://purl.org/adms/licencetype/NominalCost",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Nominal cost"
                    }
                ]
            },
            {
                "@id": "http://purl.org/adms/licencetype/GrantBack",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Grant back"
                    }
                ]
            },
            {
                "@id": "http://purl.org/adms/licencetype/JurisdictionWithinTheEU",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Jurisdiction within the EU"
                    }
                ]
            },
            {
                "@id": "http://purl.org/adms/licencetype/OtherRestrictiveClauses",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Other restrictive clauses"
                    }
                ]
            },
            {
                "@id": "http://purl.org/adms/licencetype/KnownPatentEncumbrance",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Known patent encumbrance"
                    }
                ]
            },
            {
                "@id": "http://purl.org/adms/licencetype/UnknownIPR",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Unknown IPR"
                    }
                ]
            }
        ];
        $scope.distributionTypes = [
            {
                "@id": "http://publications.europa.eu/resource/authority/distribution-type/FEED_INFO",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Information feed"
                    }
                ]
            },
            {
                "@id": "http://publications.europa.eu/resource/authority/distribution-type/WEB_SERVICE",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Web service"
                    }
                ]
            },{
                "@id": "http://publications.europa.eu/resource/authority/distribution-type/DOWNLOADABLE_FILE",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Downloadable file"
                    }
                ]
            },{
                "@id": "http://publications.europa.eu/resource/authority/documentation-type/VISUALIZATION",
                "http://www.w3.org/2004/02/skos/core#prefLabel": [
                    {
                        "@language": "en",
                        "@value": "Visualization"
                    }
                ]
            }
        ];
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

      $scope.dialog = {} ;
      $scope.dialog.languages = [] ;

      // Define the save function.
      $service.onStore = function () {
          saveDialog();
      }

      // Load data.
      loadDialog();

    }
    //

    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
