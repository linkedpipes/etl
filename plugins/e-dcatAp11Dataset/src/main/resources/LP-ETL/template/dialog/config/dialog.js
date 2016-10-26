define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://etl.linkedpipes.com/resource/components/e-dcatAp11Dataset/",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "datasetIRI": {
            "$type": "iri",
            "$label": "Dataset IRI"
        },
        "titles" : {
            "$type": "value",
            "$array": true,
            "$label": "Title"
        },
        "descriptions" : {
            "$type": "value",
            "$array": true,
            "$label": "Description"
        },
        "contactPointTypeIRI" : {
            "$type": "str",
            "$label": "Contact point type"
        },
        "contactPointName" : {
            "$type": "str",
            "$label": "Contact point name"
        },
        "contactPointEmail" : {
            "$type": "str",
            "$label": "Contact point email"
        },
        "keywords" : {
            "$type": "value",
            "$array": true,
            "$label": "Keywords"
        },
        "euThemeIRI" : {
            "$type": "str",
            "$label": "EU theme"
        },
        "otherThemeIRIs" : {
            "$type": "value",
            "$array": true,
            "$label": "Other themes"
        },
        "publisherIRI" : {
            "$type": "str",
            "$label": "Publisher"
        },
        "publisherNames" : {
            "$type": "value",
            "$array": true,
            "$label": "Publisher names"
        },
        "publisherTypeIRI" : {
            "$type": "str",
            "$label": "Publisher type"
        },
        "languages" : {
            "$onLoad" : languagesOnLoad,
            "$onSave" : languagesOnSave,
            "$type": "iri",
            "$array": true,
            "$label": "Languages"
        },
        "accrualPeriodicityIRI" : {
            "$type": "str",
            "$label": "Periodicity"
        },
        "issued" : {
            "$type": "date",
            "$label": "Issued"
        },
        "modifiedNow" : {
            "$type": "bool",
            "$label": "Modified now"
        },
        "modified" : {
            "$type": "date",
            "$label": "Modified date"
        },
        "spatialIRIs" : {
            "$type": "value",
            "$array": true,
            "$label": "Spatial"
        },
        "temporalStart" : {
            "$type": "date",
            "$label": "Temporal start"
        },
        "temporalEnd" : {
            "$type": "date",
            "$label": "Temporal end"
        },
        "documentationIRIs" : {
            "$type": "value",
            "$array": true,
            "$label": "Documentation"
        },
        "accessRightsIRI" : {
            "$type": "str",
            "$label": "Access rights"
        },
        "identifier" : {
            "$type": "str",
            "$label": "Identifier"
        },
        "datasetTypeIRI" : {
            "$type": "str",
            "$label": "Dataset type"
        },
        "provenances" : {
            "$type": "value",
            "$array": true,
            "$label": "Provenance statement"
        },
        "catalogIRI" : {
            "$type": "str",
            "$label": "Catalog"
        },
        "sampleIRIs" : {
            "$type": "value",
            "$array": true,
            "$label": "Samples"
        },
        "landingPageIRIs" : {
            "$type": "value",
            "$array": true,
            "$label": "Loading pages"
        },
        "relatedIRIs" : {
            "$type": "value",
            "$array": true,
            "$label": "Related resource"
        },
        "conformsToIRIs" : {
            "$type": "value",
            "$array": true,
            "$label": "Conforms to IRI"
        },
        "sourceIRIs" : {
            "$type": "value",
            "$array": true,
            "$label": "Source dataset IRIs"
        },
        "hasVersionIRIs" : {
            "$type": "value",
            "$array": true,
            "$label": "Has version Dataset IRIs"
        },
        "isVersionOfIRIs" : {
            "$type": "value",
            "$array": true,
            "$label": "s version of Dataset IRIs"
        },
        "version" : {
            "$type": "str",
            "$label": "Version number"
        },
        "versionNotes" : {
            "$type": "value",
            "$array": true,
            "$label": "Version notes"
        },
        "attributeIRIs" : {
            "$type": "value",
            "$array": true,
            "$label": "Attribute IRIs"
        },
        "dimensionIRIs" : {
            "$type": "value",
            "$array": true,
            "$label": "Dimension IRIs"
        },
        "numSeries" : {
            "$type": "int",
            "$label": "Number of data series"
        },
        "qualityAnnotationIRIs" : {
            "$type": "value",
            "$array": true,
            "$label": "Quality annotation IRIs"
        },
        "unitOfMeasurementIRIs" : {
            "$type": "value",
            "$array": true,
            "$label": "Unit of measurement IRIs"
        }
    };

    const LANGUAGES = [
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/AFR",
            "value": "afrikaans"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/AMH",
            "value": "amharic"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ARA",
            "value": "arabic"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ARO",
            "value": "araona"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/AYM",
            "value": "aymara"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/AYO",
            "value": "ayoreo"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/AZE",
            "value": "azerbaijani"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/BEL",
            "value": "belarusian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/BEN",
            "value": "bengali"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/BIS",
            "value": "bislama"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/BOD",
            "value": "tibetan"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/BOS",
            "value": "bosnian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/BRE",
            "value": "breton"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/BRG",
            "value": "baure"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/BUL",
            "value": "bulgarian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CAL",
            "value": "carolinian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CAO",
            "value": "chacobo"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CAS",
            "value": "chimane"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CAT",
            "value": "catalan"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CAV",
            "value": "cavineña"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CAW",
            "value": "kallawaya"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CAX",
            "value": "chiquitano"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CAZ",
            "value": "canichana"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CES",
            "value": "czech"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CHA",
            "value": "chamorro"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CHN",
            "value": "chinook jargon"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CKB",
            "value": "central kurdish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CMN",
            "value": "mandarin chinese"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/COR",
            "value": "cornish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/COS",
            "value": "corsican"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CRS",
            "value": "seselwa creole french"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CSB",
            "value": "kashubian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CYB",
            "value": "cayubaba"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CYM",
            "value": "welsh"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/DAN",
            "value": "danish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/DEU",
            "value": "german"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/DIV",
            "value": "dhivehi"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/DSB",
            "value": "lower sorbian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/DZO",
            "value": "dzongkha"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ELL",
            "value": "greek"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ENG",
            "value": "english"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/EPO",
            "value": "esperanto"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ESE",
            "value": "ese ejja"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/EST",
            "value": "estonian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/EUS",
            "value": "basque"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/FAO",
            "value": "faroese"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/FAS",
            "value": "persian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/FIJ",
            "value": "fijian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/FIL",
            "value": "filipino"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/FIN",
            "value": "finnish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/FRA",
            "value": "french"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/FRY",
            "value": "frisian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/FUR",
            "value": "friulan"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/GLA",
            "value": "scottish gaelic"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/GLE",
            "value": "irish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/GLG",
            "value": "galician"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/GLV",
            "value": "manx"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/GRN",
            "value": "guarani"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/GUG",
            "value": "paraguayan guaraní"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/GYR",
            "value": "guarayu"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/HAT",
            "value": "haitian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/HBS",
            "value": "serbo-croatian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/HCA",
            "value": "andaman creole hindi"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/HEB",
            "value": "hebrew"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/HIF",
            "value": "fiji hindi"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/HIN",
            "value": "hindi"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/HMO",
            "value": "hiri motu"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/HRV",
            "value": "croatian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/HSB",
            "value": "upper sorbian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/HUN",
            "value": "hungarian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/HYE",
            "value": "armenian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/IGN",
            "value": "ignaciano"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/INA",
            "value": "interlingua"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/IND",
            "value": "indonesian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ISL",
            "value": "icelandic"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ITA",
            "value": "italian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ITE",
            "value": "itene"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ITO",
            "value": "itonama"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/JPN",
            "value": "japanese"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/KAL",
            "value": "greenlandic"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/KAT",
            "value": "georgian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/KAZ",
            "value": "kazakh"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/KHM",
            "value": "khmer"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/KIN",
            "value": "kinyarwanda"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/KIR",
            "value": "kyrgyz"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/KMR",
            "value": "northern kurdish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/KON",
            "value": "kikongo"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/KOR",
            "value": "korean"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/KUR",
            "value": "kurdish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/KXD",
            "value": "brunei malay"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/LAO",
            "value": "lao"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/LAT",
            "value": "latin"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/LAV",
            "value": "latvian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/LEC",
            "value": "leco"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/LIN",
            "value": "lingala"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/LIT",
            "value": "lithuanian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/LTZ",
            "value": "luxembourgish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/LUA",
            "value": "luba-lulua"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MAH",
            "value": "marshallese"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MIN",
            "value": "minangkabau"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MIS",
            "value": "uncoded languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MKD",
            "value": "macedonian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MLG",
            "value": "malagasy"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MLT",
            "value": "maltese"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MON",
            "value": "mongolian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MPD",
            "value": "machinere"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MRI",
            "value": "māori"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MTP",
            "value": "wichí lhamtés nocten"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MUL",
            "value": "multiple languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MYA",
            "value": "burmese"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MZP",
            "value": "movima"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/NAU",
            "value": "nauruan"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/NBL",
            "value": "southhern ndebele"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/NEP",
            "value": "nepali"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/NIU",
            "value": "niuean"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/NLD",
            "value": "dutch"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/NNO",
            "value": "norwegian nynorsk"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/NOB",
            "value": "norwegian bokmål"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/NOR",
            "value": "norwegian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/NSO",
            "value": "northern sotho"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/NYA",
            "value": "chewa"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/OCI",
            "value": "occitan"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/OSS",
            "value": "ossetic"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/PAP",
            "value": "papiamento"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/PAU",
            "value": "palauan"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/PCP",
            "value": "pacahuara"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/PIH",
            "value": "pitcairn-norfolk"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/POL",
            "value": "polish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/POR",
            "value": "portuguese"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/PRS",
            "value": "dari"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/PSM",
            "value": "pauserna"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/PUQ",
            "value": "puquina"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/PUS",
            "value": "pashto"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/QUE",
            "value": "quechua"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/RAR",
            "value": "rarotongan"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/REY",
            "value": "reyesano"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ROH",
            "value": "romansh"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ROM",
            "value": "romani"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/RON",
            "value": "romanian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/RUN",
            "value": "kirundi"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/RUS",
            "value": "russian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SAG",
            "value": "sango"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SCO",
            "value": "scots"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SDH",
            "value": "southern kurdish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SIN",
            "value": "sinhala"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SIP",
            "value": "sikkimese"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SJE",
            "value": "pite sami"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SLK",
            "value": "slovak"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SLV",
            "value": "slovenian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SME",
            "value": "northern sami"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SMO",
            "value": "samoan"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SOM",
            "value": "somali"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SOT",
            "value": "southern sotho"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SPA",
            "value": "spanish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SQI",
            "value": "albanian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SRP",
            "value": "serbian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SRQ",
            "value": "sirionó"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SSW",
            "value": "swazi"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SWA",
            "value": "swahili"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SWB",
            "value": "comorian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SWE",
            "value": "swedish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TAM",
            "value": "tamil"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TET",
            "value": "tetum"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TGK",
            "value": "tajik"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/THA",
            "value": "thai"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TIR",
            "value": "tigrinya"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TKL",
            "value": "tokelauan"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TNA",
            "value": "tacana"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TNO",
            "value": "toromono"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TON",
            "value": "tongan"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TPI",
            "value": "tok pisin"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TPJ",
            "value": "tapieté"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TRN",
            "value": "trinitario"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TSN",
            "value": "tswana"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TSO",
            "value": "tsonga"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TUK",
            "value": "turkmen"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TUR",
            "value": "turkish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TVL",
            "value": "tuvaluan"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/UKR",
            "value": "ukrainian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/UND",
            "value": "undetermined"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/URD",
            "value": "urdu"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/URE",
            "value": "uru"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/UZB",
            "value": "uzbek"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/VEN",
            "value": "venda"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/VIE",
            "value": "vietnamese"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/VLS",
            "value": "flemish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/WLN",
            "value": "walloon"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ZHO",
            "value": "chinese"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/0D0",
            "value": "valencian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/0E0",
            "value": "montenegrin"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/AFA",
            "value": "afro-asiatic languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ALG",
            "value": "algonquian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/APA",
            "value": "apache languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ART",
            "value": "artificial languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ATH",
            "value": "athapascan languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/AUS",
            "value": "australian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/BAD",
            "value": "banda languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/BAI",
            "value": "bamileke languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/BAT",
            "value": "baltic languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/BER",
            "value": "berber languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/BNT",
            "value": "bantu languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/BTK",
            "value": "batak languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CAI",
            "value": "central american indian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CAU",
            "value": "caucasian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CEL",
            "value": "celtic languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CMC",
            "value": "chamic languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/CUS",
            "value": "cushitic languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/DAY",
            "value": "land dayak languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/DRA",
            "value": "dravidian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/FIU",
            "value": "finno-ugrian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/GEM",
            "value": "germanic languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/IJO",
            "value": "ijo languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/INC",
            "value": "indic languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/INE",
            "value": "indo-european languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/IRA",
            "value": "iranian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/IRO",
            "value": "iroquoian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/KAR",
            "value": "karen languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/KHI",
            "value": "khoisan languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/KRO",
            "value": "kru languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MAP",
            "value": "austronesian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MKH",
            "value": "mon-khmer languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MNO",
            "value": "manobo languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MOL",
            "value": "moldavian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MSA",
            "value": "malaysian"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MUN",
            "value": "munda languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/MYN",
            "value": "mayan languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/NAH",
            "value": "nahuatl languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/NAI",
            "value": "north american indian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/NIC",
            "value": "niger-kordofanian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/NUB",
            "value": "nubian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/OP_DATPRO",
            "value": "provisional data"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/OTO",
            "value": "otomian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/PAA",
            "value": "papuan languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/PHI",
            "value": "philippine languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/PRA",
            "value": "prakrit languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ROA",
            "value": "romance languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SAI",
            "value": "south american indian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SAL",
            "value": "salishan languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SEM",
            "value": "semitic languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SGN",
            "value": "sign languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SIO",
            "value": "siouan languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SIT",
            "value": "sino-tibetan languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SLA",
            "value": "slavic languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SMI",
            "value": "sami languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SON",
            "value": "songhai languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/SSA",
            "value": "nilo-saharan languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TAI",
            "value": "tai languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TUP",
            "value": "tupi languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/TUT",
            "value": "altaic languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/WAK",
            "value": "wakashan languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/WEN",
            "value": "sorbian languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/XHO",
            "value": "xhosa"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/YAA",
            "value": "yaminava"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/YID",
            "value": "yiddish"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/YPK",
            "value": "yupik languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/YUE",
            "value": "yue chinese"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/YUQ",
            "value": "yuqui"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/YUZ",
            "value": "yuracare"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ZLM",
            "value": "malay"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ZND",
            "value": "zande languages"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ZUL",
            "value": "zulu"
        },
        {
            "IRI": "http://publications.europa.eu/resource/authority/language/ZXX",
            "value": "no linguistic content"
        },
    ];

    const PUBLISHER_TYPES = [
        {
            "IRI": "http://purl.org/adms/publishertype/Academia-ScientificOrganisation",
            "label": "Academia/Scientific organisation"
        },
        {
            "IRI": "http://purl.org/adms/publishertype/Company",
            "label": "Company"
        },
        {
            "IRI": "http://purl.org/adms/publishertype/IndustryConsortium",
            "label": "Industry consortium"
        },
        {
            "IRI": "http://purl.org/adms/publishertype/LocalAuthority",
            "label": "Local Authority"
        },
        {
            "IRI": "http://purl.org/adms/publishertype/NationalAuthority",
            "label": "National authority"
        },
        {
            "IRI": "http://purl.org/adms/publishertype/NonGovernmentalOrganisation",
            "label": "Non-Governmental Organisation"
        },
        {
            "IRI": "http://purl.org/adms/publishertype/NonProfitOrganisation",
            "label": "Non-Profit Organisation"
        },
        {
            "IRI": "http://purl.org/adms/publishertype/PrivateIndividual(s)",
            "label": "Private Individual(s)"
        },
        {
            "IRI": "http://purl.org/adms/publishertype/RegionalAuthority",
            "label": "Regional authority"
        },
        {
            "IRI": "http://purl.org/adms/publishertype/StandardisationBody",
            "label": "Standardisation body"
        },
        {
            "IRI": "http://purl.org/adms/publishertype/SupraNationalAuthority",
            "label": "Supra-national authority"
        }
    ]

    const FREQUENCIES = [
        {
            "@id": "http://publications.europa.eu/resource/authority/frequency/ANNUAL",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "annual"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/ANNUAL_2",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "semiannual"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/ANNUAL_3",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "three times a year"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/BIENNIAL",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "biennial"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/BIMONTHLY",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "bimonthly"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/BIWEEKLY",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "biweekly"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/CONT",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "continuous"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/DAILY",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "daily"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/DAILY_2",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "twice a day"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/IRREG",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "irregular"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/MONTHLY",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "monthly"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/MONTHLY_2",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "semimonthly"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/MONTHLY_3",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "three times a month"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/NEVER",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "never"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/OP_DATPRO",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "Provisional data"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/OTHER",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "other"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/QUARTERLY",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "quarterly"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/TRIENNIAL",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "triennial"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/UNKNOWN",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "unknown"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/UPDATE_CONT",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "continuously updated"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/WEEKLY",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "weekly"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/WEEKLY_2",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "semiweekly"
            }]
        }, {
            "@id": "http://publications.europa.eu/resource/authority/frequency/WEEKLY_3",
            "http://www.w3.org/2004/02/skos/core#prefLabel": [{
                "@language": "en",
                "@value": "three times a week"
            }]
        }];

    const EU_THEMES = [
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

    const ACCESS_RIGHTS = [
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

    const DATASET_TYPES = [
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
    ];

    function languagesOnLoad(values) {
        let result = [];
        values.forEach((iri) => {
            let label = undefined;
            for (let i = 0;  i < LANGUAGES.length; ++i) {
                if (LANGUAGES[i].IRI === iri) {
                    label = LANGUAGES[i].value;
                    break;
                }
            }
            if (label === undefined) {
                console.error("Missing language for IRI: ", iri);
            }
            result.push({
                'IRI' : iri,
                'value' : label
            });
        });
        return result;
    }

    function languagesOnSave(values) {
        let result = [];
        values.forEach((value) => {
           result.push(value['IRI']);
        });
        return result;
    }

    function controller($scope, $service) {

        if ($scope.dialog === undefined) {
            $scope.dialog = {};
        }

        const dialogManager = $service.v1.manager(DESC, $scope.dialog);

        $service.onStore = function () {
            dialogManager.save();
        };

        $scope.createLangFilter = function createFilterFor(query) {
            let lowercaseQuery = angular.lowercase(query);
            return function filterFn(language) {
                return (language.value.indexOf(lowercaseQuery) !== -1);
            };
        };

        $scope.langSearch = function (query) {
            if (query) {
                return $scope.languages.filter($scope.createLangFilter(query));
            } else {
                return $scope.languages;
            }
        };

        $scope.transformChip = function (chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return {value: chip, IRI: 'new'};
        };

        $scope.languages = LANGUAGES;
        $scope.publishertypes = PUBLISHER_TYPES;
        $scope.frequencies = FREQUENCIES;
        $scope.euThemes = EU_THEMES;
        $scope.accessRights = ACCESS_RIGHTS;
        $scope.datasetTypes = DATASET_TYPES;

        dialogManager.load();
    }

    controller.$inject = ['$scope', '$service'];
    return controller;
});
