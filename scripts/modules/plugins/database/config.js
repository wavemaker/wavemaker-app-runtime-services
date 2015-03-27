/*global WM, wm*/

/*Defining module for Database services*/
wm.plugins.database = WM.module('wm.plugins.database', []);

/*Creating namespaces for the controllers, services etc. of the module*/
wm.plugins.database.directives = {};
wm.plugins.database.controllers = {};
wm.plugins.database.services = {};
wm.plugins.database.factories = {};

/*Defining the controllers, services etc. required for the Database services module*/
wm.plugins.database.directive(wm.plugins.database.directives);
wm.plugins.database.controller(wm.plugins.database.controllers);
wm.plugins.database.service(wm.plugins.database.services);
wm.plugins.database.factory(wm.plugins.database.factories);

/*defining urls as constants in the database services module*/
wm.plugins.database.constant('DB_SERVICE_URLS', {
    Database: {
        importSample: {
            url: "../services/databaseservice/actions",
            method: "POST"
        },
        testConnection: {
            url: "../services/databaseservice/actions",
            method: "POST"
        },
        importDB: {
            url: "../services/databaseservice/actions",
            method: "POST"
        },
        reImportDB: {
            url: "../services/databaseservice/actions",
            method: "POST"
        },
        exportDB: {
            url: "../services/databaseservice/actions",
            method: "POST"
        },
        executeDBScript: {
            url: "../services/databaseservice/executescript",
            method: "POST",
            headers: {
                'Content-Type': undefined
            },
            transformRequest: WM.identity
        },

        /*DataModel related services*/
        getAllDataModels: {
            url: "../services/projects/:projectID/datamodels/",
            method: "GET"
        },
        getDataModel: {
            url: "../services/projects/:projectID/datamodels/:dataModelName?isdraft=:isdraft",
            method: "GET"
        },
        createDataModel: {
            url: "../services/projects/:projectID/datamodels",
            method: "POST"
        },
        deleteDataModel: {
            url: "../services/projects/:projectID/datamodels/:dataModelName",
            method: "DELETE"
        },
        saveDataModel: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/save",
            method: "POST"
        },
        applyDataModel: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/apply",
            method: "POST"
        },
        revertDataModel: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/revert",
            method: "POST"
        },

        /*Entities related services*/

        getAllEntities: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/tables",
            method: "GET"
        },
        getEntity: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/tables/:tableName",
            "method": "GET"
        },
        createEntity: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/tables",
            method: "POST"
        },
        updateEntity: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/tables/:entityName",
            method: "POST"
        },
        deleteEntity: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/tables/:entityName",
            method: "DELETE"
        },

        /*Entity Columns related services*/

        addColumns: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/tables/:entityName/columns",
            method: "POST"
        },
        updateColumn: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/tables/:entityName/columns/:columnName",
            method: "PUT"
        },
        deleteColumn: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/tables/:entityName/columns/:columnName",
            method: "DELETE"
        },

        /*Services related to relations*/

        addRelation: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/tables/:entityName/relations",
            method: "POST"
        },
        updateRelation: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/tables/:entityName/relations/:relationName",
            method: "PUT"
        },
        deleteRelation: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/tables/:entityName/relations/:relationName",
            method: "DELETE"
        },

        /*Queries Related services*/

        getAllQueries: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/queries",
            method: "GET"
        },
        getQuery: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/queries/:queryName",
            method: "GET"
        },
        createQuery: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/queries",
            method: "POST"
        },
        updateQuery: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/queries/:queryName",
            method: "PUT"
        },
        deleteQuery: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/queries/:queryName",
            method: "DELETE"
        },
        setQueryMetaData: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/queries/:queryName/metadata",
            method: "POST"
        },
        validateQuery: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/query/validate",
            method: "POST"
        },
        executeQuery: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/query/execute",
            method: "POST"
        },

        /*Procedure Related services*/

        getAllProcedures: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/procedures",
            method: "GET"
        },
        getProcedure: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/procedures/:procedureName",
            method: "GET"
        },
        createProcedure: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/procedures",
            method: "POST"
        },
        updateProcedure: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/procedures/:procedureName",
            method: "PUT"
        },
        deleteProcedure: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/procedures/:procedureName",
            method: "DELETE"
        },
        setProcedureMetaData: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/procedures/:procedureName/metadata",
            method: "POST"
        },
        executeProcedure: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/procedures/execute",
            method: "POST"
        },


        readTableData: {
            url: "/:service/:dataModelName/:entityName/?page=:page&size=:size&:sort",
            method: "GET"
        },
        insertTableData: {
            url: "/:service/:dataModelName/:entityName/",
            method: "POST"
        },
        insertMultiPartTableData: {
            url: "/:service/:dataModelName/:entityName/",
            method: "POST",
            headers: {
                'Content-Type': undefined
            },
            transformRequest: WM.identity
        },
        updateTableData: {
            url: "/:service/:dataModelName/:entityName/:id",
            method: "PUT"
        },
        updateMultiPartTableData: {
            url: "/:service/:dataModelName/:entityName/:id",
            method: "POST",
            headers: {
                'Content-Type': undefined
            },
            transformRequest: WM.identity
        },
        deleteTableData: {
            url: "/:service/:dataModelName/:entityName/:id",
            method: "DELETE"
        },
        updateCompositeTableData: {
            url: "/:service/:dataModelName/:entityName/composite-id?:id",
            method: "PUT"
        },
        deleteCompositeTableData: {
            url: "/:service/:dataModelName/:entityName/composite-id?:id",
            method: "DELETE"
        },
        searchTableData: {
            url: "/:service/:dataModelName/:entityName/search?page=:page&size=:size",
            method: "POST"
        },
        readTableRelatedData: {
            url: "/:service/:dataModelName/:entityName/:id/:relatedFieldName?page=:page&size=:size&:sort",
            method: "GET"
        },
        executeNamedQuery: {
            url: "/:service/:dataModelName/queryExecutor/queries/:queryName?page=:page&size=:size&:queryParams",
            method: "GET"
        },
        executeCustomQuery: {
            url: "/:service/:dataModelName/queryExecutor/queries/wm_custom?page=:page&size=:size",
            method: "POST"
        },
        getCustomQueryMetaData: {
            url: "/:service/:dataModelName/queries/wm_querymetadata",
            method: "POST"
        },
        executeCustomUpdateQuery: {
            url: "/:service/:dataModelName/queryExecutor/queries/wm_custom_update",
            method: "POST"
        },

        /*Procedure related properties*/
        executeNamedProcedure: {
            url: "/:service/:dataModelName/procedureExecutor/procedure/execute/:procedureName?page=:page&size=:size&:procedureParams",
            method: "GET"
        },
        executeCustomProcedure: {
            url: "/:service/:dataModelName/procedureExecutor/procedure/execute/wm_custom",
            method: "POST"
        },
        getCustomProcedureMetaData: {
            url: "/:service/:dataModelName/procedures/wm_proceduremetadata",
            method: "POST"
        },

        /*Database connection properties*/

        getConnectionProperties: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/connectionproperties",
            method: "GET"
        },
        updateConnectionProperties: {
            url: "../services/projects/:projectID/datamodels/:dataModelName/connectionproperties",
            method: "PUT"
        }
    }
});

/*Defining the constants for the database service module*/
wm.plugins.database.constant('DB_CONSTANTS', {
    "DATABASE_WORKSPACE_TYPE": "db",
    "WAVEMAKER_SAMPLE_DB_NAME": "hrdb",
    "DEFAULT_DB_NAME": "MyTestDatabase",
    "HSQL_DB_TYPE": "HSQLDB",
    "MYSQL_DB_TYPE": "MySQL",
    "POSTGRESQL_DB_TYPE": "PostgreSQL",
    "ORACLE_DB_TYPE": "Oracle",
    "SQL_SERVER_DB_TYPE": "SQLServer",
    "LOGIN": {
        "SAAS_MYSQL_CLOUD": {
            "USERNAME": "root",
            "PASSWORD": "cloudjee123"
        }
    },
    "DATABASE_NUMERIC_DATA_TYPES": {
        "big_decimal": "big_decimal",
        "big_integer": "big_integer",
        "double": "double",
        "float": "float",
        "integer": "integer"
    },
    "DATABASE_DATA_TYPES": {
        "big_decimal": {
            "sql_type": "big_decimal",
            "default_value": "0"
        },
        "big_integer": {
            "sql_type": "big_integer",
            "default_value": "0"
        },
        "blob": {
            "sql_type": "blob",
            "default_value": "null"
        },
        "boolean": {
            "sql_type": "boolean",
            "default_value": "false"
        },
        "byte": {
            "sql_type": "byte",
            "default_value": "null"
        },
        "character": {
            "sql_type": "character",
            "default_value": "null"
        },
        "clob": {
            "sql_type": "clob",
            "default_value": "null"
        },
        "date": {
            "sql_type": "date",
            "default_value": "null"
        },
        "double": {
            "sql_type": "double",
            "default_value": "0"
        },
        "float": {
            "sql_type": "float",
            "default_value": "0"
        },
        "integer": {
            "sql_type": "integer",
            "default_value": "0"
        },
        "string": {
            "sql_type": "string",
            "default_value": "null"
        },
        "short": {
            "sql_type": "short",
            "default_value": "0"
        },
        "text": {
            "sql_type": "text",
            "default_value": "null"
        },
        "time": {
            "sql_type": "time",
            "default_value": "null"
        },
        "timestamp": {
            "sql_type": "timestamp",
            "default_value": "null"
        }
    },
    "DATABASE_SECONDARY_DATA_TYPES": {
        "binary": {
            "sql_type": "blob",
            "default_value": "null"
        },
        "long": {
            "sql_type": "double",
            "default_value": "null"
        }
    },
    "DATABASE_GENERATORS": {
        "assigned": "assigned",
        "identity": "auto increment",
        "sequence": "sequence"
    },
    "DATABASE_MATCH_MODES": {
        "start": "STARTING_WITH",
        "end": "ENDING_WITH",
        "anywhere": "CONTAINING",
        "exact": "EQUALS"
    },
    "ACTIONS": {
        "CREATE": "CREATE",
        "UPDATE": "UPDATE",
        "DELETE": "DELETE",
        "SAVED": "SAVED"
    },
    "OBJECTS": {
        "DATABASE": "wm-db",
        "TABLE": "wm-db-table",
        "COLUMN": "wm-db-table-column",
        "RELATION": "wm-db-column-relation"
    },
    "DML_QUERY_INDICATORS": {
        "SELECT": "SELECT",
        "UPDATE": ["INSERT INTO", "UPDATE", "DELETE FROM"]
    },
    "DDL_QUERY_INDICATORS": ["CREATE", "ALTER", "RENAME", "DROP"]
});

/*Defining the config for the database plugins*/
wm.plugins.database.config(function (BaseServiceManagerProvider, DB_SERVICE_URLS) {
    'use strict';

    BaseServiceManagerProvider.register(DB_SERVICE_URLS);
});
