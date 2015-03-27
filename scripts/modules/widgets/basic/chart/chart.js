/*global WM, nv, d3, _ */
/*Directive for chart */

WM.module('wm.widgets.basic')
    .run(["$templateCache", '$rootScope', function ($templateCache, $rootScope) {
        "use strict";
        $templateCache.put("template/widget/form/chart.html",
            '<div init-widget class="app-chart" title="{{hint}}" data-ng-show="show" ' + $rootScope.getWidgetStyles() + ' >' +
                '<svg></svg>' +
                '<div class="wm-content-info readonly-wrapper {{class}}" data-ng-show="showContentLoadError"><p class="wm-message" title="{{hintMsg}}">{{errMsg}}</p></div>' +
            '</div>'
            );
    }]).directive('wmChart', function (PropertiesFactory, $templateCache, $rootScope, WidgetUtilService, CONSTANTS, Variables, QueryBuilder, Utils, $timeout) {
        "use strict";
        var widgetProps = PropertiesFactory.getPropertiesOf("wm.chart", ["wm.base"]),
            themes = {
                'Terrestrial': {
                    colors: ["#1f77b4", "#aec7e8", "#ff7f0e", "#ffbb78", "#2ca02c", "#98df8a", "#d62728", "#ff9896", "#9467bd", "#c5b0d5", "#8c564b", "#c49c94", "#e377c2", "#f7b6d2", "#7f7f7f", "#c7c7c7", "#bcbd22", "#dbdb8d", "#17becf", "#9edae5"],
                    tooltip: {
                        "backgroundColor": "#de7d28",
                        "textColor": "#FFFFFF"
                    }
                },
                'Annabelle': {
                    colors: ["#393b79", "#5254a3", "#6b6ecf", "#9c9ede", "#637939", "#8ca252", "#b5cf6b", "#cedb9c", "#8c6d31", "#bd9e39", "#e7ba52", "#e7cb94", "#843c39", "#ad494a", "#d6616b", "#e7969c", "#7b4173", "#a55194", "#ce6dbd", "#de9ed6"],
                    tooltip: {
                        "backgroundColor": "#2e306f",
                        "textColor": "#FFFFFF"
                    }
                },
                'Azure': {
                    colors: ["#3182bd", "#6baed6", "#9ecae1", "#c6dbef", "#e6550d", "#fd8d3c", "#fdae6b", "#fdd0a2", "#31a354", "#74c476", "#a1d99b", "#c7e9c0", "#756bb1", "#9e9ac8", "#bcbddc", "#dadaeb", "#636363", "#969696", "#bdbdbd", "#d9d9d9"],
                    tooltip: {
                        "backgroundColor": "#3182bd",
                        "textColor": "#FFFFFF"
                    }
                },
                'Retro': {
                    colors: ["#0ca7a1", "#ffa615", "#334957", "#acc5c2", "#988f90", "#8accc9", "#515151", "#f27861", "#36c9fd", "#794668", "#0f709d", "#0d2738", "#44be78", "#4a1839", "#6a393f", "#557d8b", "#6c331c", "#1c1c1c", "#861500", "#09562a"],
                    tooltip: {
                        "backgroundColor": "#80513a",
                        "textColor": "#FFFFFF"
                    }
                },
                'Mellow': {
                    colors: ["#f0dcbf", "#88c877", "#aeb918", "#2e2c23", "#ddddd2", "#dfe956", "#4c963b", "#5d3801", "#e1eec3", "#cd8472", "#fcfab3", "#9a4635", "#9295ad", "#2e3f12", "#565677", "#557d8b", "#4f4d02", "#0c0c1b", "#833324", "#24120e"],
                    tooltip: {
                        "backgroundColor": "#7c9e73",
                        "textColor": "#FFFFFF"
                    }
                },
                'Orient': {
                    colors: ["#a80000", "#cc6c3c", "#f0e400", "#000084", "#fccc6c", "#009c6c", "#cc309c", "#78cc00", "#fc84e4", "#48e4fc", "#4878d8", "#186c0c", "#606060", "#a8a8a8", "#000000", "#d7d7d7", "#75a06e", "#190d0b", "#888888", "#694b84"],
                    tooltip: {
                        "backgroundColor": "#c14242",
                        "textColor": "#FFFFFF"
                    }
                },
                'GrayScale': {
                    colors: ["#141414", "#353535", "#5b5b5b", "#848484", "#a8a8a8", "#c3c3c3", "#e0e0e0", "#c8c8c8", "#a5a5a5", "#878787", "#656565", "#4e4e4e", "#303030", "#1c1c1c", "#4f4f4f", "#3b3b3b", "#757575", "#606060", "#868686", "#c1c1c1"],
                    tooltip: {
                        "backgroundColor": "#575757",
                        "textColor": "#FFFFFF"
                    }
                },
                'Flyer': {
                    colors: ["#3f454c", "#5a646e", "#848778", "#cededf", "#74c4dd", "#0946ed", "#380bb1", "#000ff0", "#f54a23", "#1db262", "#bca3aa", "#ffa500", "#a86b32", "#63a18c", "#56795e", "#934343", "#b75f5f", "#752d2d", "#4e1111", "#920606"],
                    tooltip: {
                        "backgroundColor": "#47637c",
                        "textColor": "#FFFFFF"
                    }
                }
            },
           /*properties of the respective chart type*/
            options = {
                'Column': ['showcontrols', 'staggerlabels', 'reducexticks', 'barspacing', 'xaxislabel', 'yaxislabel', 'xunits', 'yunits', 'yaxislabeldistance'],
                'Line': ['xaxislabel', 'yaxislabel', 'xunits', 'yunits', 'yaxislabeldistance'],
                'Area': ['showcontrols', 'xaxislabel', 'yaxislabel', 'xunits', 'yunits', 'yaxislabeldistance'],
                'Cumulative Line': ['showcontrols', 'xaxislabel', 'yaxislabel', 'xunits', 'yunits', 'yaxislabeldistance'],
                'Bar': ['showvalues', 'showcontrols', 'xaxislabel', 'yaxislabel', 'xunits', 'yunits', 'xaxislabeldistance'],
                'Pie': ['showlabels', 'labeltype', 'showlabelsoutside'],
                'Donut': ['showlabels', 'labeltype', 'donutratio', 'showlabelsoutside'],
                'Bubble' : ['showxdistance', 'showydistance', 'bubblesize', 'tooltipcolumns', 'shape']
            },
            /*all properties of the chart*/
            allOptions = ['showvalues', 'showlabels', 'showcontrols', 'useinteractiveguideline', 'staggerlabels', 'reducexticks', 'barspacing', 'labeltype', 'donutratio', 'showlabelsoutside', 'xaxislabel', 'yaxislabel', 'xunits', 'yunits', 'xaxislabeldistance', 'yaxislabeldistance', 'showxdistance', 'showydistance', 'bubblesize', 'tooltipcolumns', 'shape'],
            advanceDataProps = ['aggregation', 'aggregationcolumn', 'groupby', 'orderby'],
            chartTypes = ["Column", "Line", "Area", "Cumulative Line", "Bar", "Pie", "Donut", "Bubble"],
            styleProps = {
                "fontunit": "font-size",
                "fontsize": "font-size",
                "color": "fill",
                "fontfamily": "font-family",
                "fontweight": "font-weight",
                "fontstyle": "font-style",
                "textdecoration": "text-decoration"
            },
            tickformats = {
                'Round to Thousand': {
                    'prefix': "K",
                    'divider': 1000
                },
                'Round to Million' : {
                    'prefix': "M",
                    'divider': 1000000
                },
                'Round to Billion' : {
                    'prefix': "B",
                    'divider': 1000000000
                }
            },
            dataTypeJSON = ['Column', 'Line', 'Pie', 'Bar', 'Donut', 'Bubble'],     /*Charts that supports the data to be JSON*/
            dataTypeArray = ['Cumulative Line', 'Area'],     /*Charts that supports the data to be Array*/
            lineTypeCharts = ['Line', 'Area', 'Cumulative Line'],   /*Charts that does not supports the string type of data in the xaxis in the nvd3*/
            allShapes = ["circle", "square", "diamond", "cross", "triangle-up", "triangle-down"],
            sampleData = {};

        /* returns true if chart type is cumulative line */
        function isCumulativeLineChart(type) {
            return (type === 'Cumulative Line');
        }

        /* returns true if chart type is pie */
        function isPieChart(type) {
            return (type === 'Pie');
        }

        /* returns true if chart type is bar */
        function isBarChart(type) {
            return (type === 'Bar');
        }

        /* returns true if chart type is donut */
        function isDonutChart(type) {
            return (type === 'Donut');
        }

        /* returns true if chart type is bubble */
        function isBubbleChart(type) {
            return (type === 'Bubble');
        }

        /* returns true if chart type is pie or donut */
        function isPieType(type) {
            return (isPieChart(type) || isDonutChart(type));
        }

        /* The format of chart data is array of json objects in case of the following types of chart */
        function isChartDataJSON(type) {
            return (dataTypeJSON.indexOf(type) > -1 || chartTypes.indexOf(type) === -1);
        }

        /* The format of chart data is array of objects in case of the following types of chart */
        function isChartDataArray(type) {
            return (dataTypeArray.indexOf(type) > -1);
        }

        /* returns true is the chart type is 'line', 'area' or 'cumulative line' else false*/
        function isLineTypeChart(type) {
            return (lineTypeCharts.indexOf(type) > -1);
        }

        /* Formatting the data based on the data-type */
        function formatData(scope, d, dataType, options) {
            var datakey = (options.isXaxis && options.xDataKeyArr && options.xDataKeyArr.length) ? options.xDataKeyArr[d] : d,
                formattedData,
                divider;
            switch (dataType) {
            case 'date':
                return !isLineTypeChart(scope.type) ? d3.time.format(options.dateFormat)(new Date(d)) : datakey;
            case 'string':
            case 'year':
                return datakey;
            default:
                formattedData = d3.format(options.format)(d);
                /** formating the data based on number format selected **/
                if (options.numberFormat && dataType) {
                    /*Getting the respective divider[1000,1000000,1000000000] based on the number format choosen*/
                    divider = (tickformats[options.numberFormat] && tickformats[options.numberFormat].divider) || 0;
                    if (isPieType(scope.type)) {
                        formattedData = d + tickformats[options.numberFormat].prefix;
                    } else if (divider !== 0) {
                        /*Dividing the value with respective divider*/
                        formattedData = d3.format('.2f')(d / divider) + tickformats[options.numberFormat].prefix;
                    }
                } else if (!options.numberFormat) {
                    /*Auto formatting the data when no formating option is chosen*/
                    formattedData = d3.format('.3s')(d);
                }
                return formattedData;
            }
        }

        /*Construct the sample data*/
        function constructSampleData(scope) {
            var i,
                jsonFormatData = [],
                pieChartData = [],
                arrayFormatData = [],
                pieName = 'Group',
                dataPoint;

            for (i = 1; i < 5; i += 1) {
                dataPoint = {};
                pieChartData.push({x: pieName + i, y: i});
                dataPoint.x = i;
                dataPoint.y = i;
                /*Only bubble type of the chart has the size attribute */
                if (isBubbleChart(scope.type)) {
                    dataPoint.size =  (i + 1) * 2;
                    dataPoint.shape =  scope.shape || 'circle';
                }
                jsonFormatData.push(dataPoint);
                arrayFormatData.push([i, i]);

            }
            sampleData.jsonFormat = jsonFormatData;
            sampleData.arrayFormat = arrayFormatData;
            sampleData.pieChartFormat = pieChartData;
        }


        /** Sample data to populate when no data is bound*/
        function getSampleData(scope) {
            var dataType;
            if (Utils.isEmptyObject(sampleData) ||  isBubbleChart(scope.type)) {
                constructSampleData(scope);
            }

            if (isPieType(scope.type)) {
                return sampleData.pieChartFormat;
            }

            dataType = isChartDataJSON(scope.type) ? "jsonFormat" : "arrayFormat";
            return [{values: sampleData[dataType], key: 'Default'}];
        }

        /* get the data type for the service variable type*/
        function getDataType(key, data) {
            var keys = key.split('.'),
                newKey = key;
            if (data) {
                var value = data[key];
                /*If the element is not directly accessible then access it inside of it*/
                if (value === undefined && keys.length > 1) {
                    data = data[keys[0]];
                    keys.shift();
                    newKey = keys.join('.');
                    return getDataType(newKey, data);
                }
                return typeof value;
            }
            return null;
        }

        /* get the column type definition for the live data-source*/
        function getColumnType(key, columns) {
            var keys = key.split('.'),
                newKey,
                i,
                type;
            if (columns) {
                for (i = 0; i < columns.length; i += 1) {
                    /*Trying to get the column type of key fields of object columns*/
                    if (keys.length > 0 && key.indexOf('.') > -1) {
                        if (Utils.initCaps(keys[0]) === columns[i].relatedEntityName) {
                            /*initialising columns with columns of object type column*/
                            columns = columns[i].columns;
                            /*removing already accessed keys*/
                            keys.shift();
                            newKey = keys.join('.');
                            return getColumnType(newKey, columns);
                        }
                    } else if (columns[i].fieldName === key) {
                        type = columns[i].type;
                    }
                }
            }
            return type || null;
        }

        /* Hide the properties that are passed to it*/
        function hideOrShowProperties(properties, scope, show) {
            /* sanity check */
            show = WM.isDefined(show) ? show : false;

            var _widgetProps = scope.widgetProps;
            properties.forEach(function (prop) {
                _widgetProps[prop].show = show;
            });
        }

        /* Configuring the properties panel based on the type of the chart chosen*/
        function togglePropertiesByChartType(scope) {
            /* Initially hiding all the properties*/
            hideOrShowProperties(allOptions, scope, false);
            /* Showing the properties based on the type of the chart*/
            hideOrShowProperties((chartTypes.indexOf(scope.type) === -1) ? options['Column'] : options[scope.type] , scope, true);

            if (isPieType(scope.type)) {
                /* If pie chart, set the display key for x and y axis datakey and subgroups */
                scope.widgetProps.xaxisdatakey.displayKey = 'LABEL_PROPERTY_LABEL';
                scope.widgetProps.yaxisdatakey.displayKey = 'LABEL_PROPERTY_VALUES';
                PropertiesFactory.getPropertyGroup('xaxis').displayKey = 'LABEL_PROPERTY_LABEL';
                PropertiesFactory.getPropertyGroup('yaxis').displayKey = 'LABEL_PROPERTY_VALUES';

                /* If it is a pie chart then the yaxisdatakey must be a single select else it has to be a multiselect */
                scope.widgetProps.yaxisdatakey.widget = 'list';
                /* Only if bound to valid dataset populate the options*/
                if (scope.dataset) {
                    scope.widgetProps.yaxisdatakey.options = scope.axisoptions;
                }
            } else {
                scope.widgetProps.xaxisdatakey.displayKey = undefined;
                scope.widgetProps.yaxisdatakey.displayKey = undefined;
                PropertiesFactory.getPropertyGroup('xaxis').displayKey = undefined;
                PropertiesFactory.getPropertyGroup('yaxis').displayKey = undefined;

                scope.widgetProps.yaxisdatakey.widget = 'multiselect';
                $timeout(function () {
                    /* TODO: to check if same options can be fed to the checkboxset */
                    scope.widgetDataset['yaxisdatakey'] = scope.axisoptions ? scope.axisoptions.join(',') : '';
                }, 5);
            }
        }

        /*
         * Displaying the formatting options based on the type of the column chosen
         * @param axis, x or y axis
         */
        function displayFormatOptions(scope, axis) {
            var type,
                key = axis + "axisdatakey",
                numFormat = axis + "numberformat",
                digits = axis + "digits",
                dateFormat = axis + "dateformat";
            /* return in case of pie/donut chart and x axis return*/
            if (isPieType(scope.type) && axis === 'x') {
                return;
            }

            /* get column type */
            if (scope.dataset && scope.dataset.propertiesMap) {
                type = scope[key] ? getColumnType(scope[key].split(',')[0], scope.dataset.propertiesMap.columns) : null;
            }
            switch (type) {
            case 'integer':
            case 'float':
                hideOrShowProperties([numFormat, digits], scope, true);
                hideOrShowProperties([dateFormat], scope, false);
                break;
            case 'string':
                hideOrShowProperties([numFormat, digits, dateFormat], scope, false);
                break;
            case 'date':
                hideOrShowProperties([numFormat, digits], scope, false);
                hideOrShowProperties([dateFormat], scope, true);
                break;
            }
        }

        /** Checks if the yaxisdatakey is a singleselect or multiselect based on chart type */
        function isSingleYAxis(type) {
            return isPieType(type) ? true : false;
        }

        /** Based on the chart type, sets the options for the yaxisdatakey*/
        function setYAxisDataKey(scope, options, dataSet) {
            if (isSingleYAxis(scope.type)) {
                scope.widgetProps.yaxisdatakey.widget = 'list';
                scope.widgetProps.yaxisdatakey.options = options;
            } else {
                scope.widgetDataset['yaxisdatakey'] = dataSet || options ? options.join(',') : '';
            }
        }

        function isGroupByEnabled(groupby) {
            return (groupby && groupby !== "none");
        }

        /* enables/disables the aggregation function property */
        function toggleAggregationState(scope) {
            scope.widgetProps.aggregation.disabled = !(isGroupByEnabled(scope.groupby));
            /* enables/disables the aggregation column property*/
            toggleAggregationColumnState(scope);
        }

        /* enables/disables the aggregation column property*/
        function toggleAggregationColumnState(scope) {
            scope.widgetProps.aggregationcolumn.disabled = !(isGroupByEnabled(scope.groupby) && scope.aggregation && scope.aggregation !== "none");
        }

        /* Displaying options for x and y axis based on the columns chosen in aggregation column and groupby*/
        function modifyAxesOptions(scope) {
            var xAxisOptions = [],
                yAxisOptions = [],
                isAggregationApplied = (isGroupByEnabled(scope.groupby) && scope.aggregation && scope.aggregation !== "none"),
                options;
            /*Check if the data-set has been bound and the value is available in data-set.*/
            if (scope.binddataset && WM.isObject(scope.dataset)) {

                /* get axis options */
                options = scope.axisoptions;
                if (isAggregationApplied) {
                    if (scope.groupby) {
                        xAxisOptions = scope.groupby.split(',');
                        scope.xaxisdatakey = xAxisOptions[0];
                    } else {
                        xAxisOptions = options;
                    }
                    /*If "aggregation" is not "none" and if the "aggregationColumn" has not already been added into the axesOptions, then add it.*/
                    if (isAggregationApplied && scope.aggregationcolumn) {
                        yAxisOptions.push(scope.aggregationcolumn);
                        scope.yaxisdatakey = yAxisOptions[0];
                    } else {
                        yAxisOptions = options;
                    }
                    scope.widgetProps.xaxisdatakey.options = xAxisOptions;
                    setYAxisDataKey(scope, yAxisOptions, '');
                    setYAxisDataKey(scope, yAxisOptions, yAxisOptions);
                    /*Setting the bubble size and tooltip columns to be shown*/
                    if (isBubbleChart(scope.type)) {
                        scope.widgetProps.bubblesize.options = options;
                        scope.widgetDataset.tooltipcolumns = scope.dataset || options ? options.join(',') : '';
                    }
                } else {
                    scope.widgetProps.xaxisdatakey.options = options;
                    setYAxisDataKey(scope, options, '');
                    if (isBubbleChart(scope.type)) {
                        scope.widgetProps.bubblesize.options = options;
                        scope.widgetDataset.tooltipcolumns = scope.dataset || options ? options.join(',') : '';
                    }
                }

                displayFormatOptions(scope, 'x');
                displayFormatOptions(scope, 'y');
            } else if (!scope.binddataset) {/*Else, set all the values to default.*/
                scope.widgetProps.xaxisdatakey.options = [];
                setYAxisDataKey(scope, [], '');
                scope.widgetProps.aggregationcolumn.options = [];
                scope.xaxisdatakey = scope.yaxisdatakey = '';
                scope.xaxislabel = scope.yaxislabel = '';
                scope.xunits = scope.yunits = '';
                scope.bubblesize = scope.tooltipcolumns = '';
                scope.widgetProps.bubblesize.options = [];
                scope.widgetProps.tooltipcolumns.options = [];
                scope.widgetProps.aggregationcolumn.disabled = true;
                scope.widgetProps.aggregation.disabled = true;
                /*Setting the values to the default*/
                $rootScope.$emit('update-widget-property', 'aggregation', "");
                $rootScope.$emit('update-widget-property', 'aggregationcolumn', "");
                $rootScope.$emit('update-widget-property', 'groupby', "");
                $rootScope.$emit('update-widget-property', 'orderby', "");
            }
            scope.$root.$emit("set-markup-attr", scope.widgetid, {'xaxisdatakey': scope.xaxisdatakey, 'yaxisdatakey': scope.yaxisdatakey});
        }

        /* Check if x and y axis that are chosen are valid to plot chart */
        function isValidAxis(scope) {
            /* Check if x axis and y axis are chosen and are not equal */
            return scope.binddataset ? (scope.xaxisdatakey && scope.yaxisdatakey) : true;
        }

        /* Check if aggregation is chosen */
        function isAggregationEnabled(scope) {
            return ((isGroupByEnabled(scope.groupby) && scope.aggregation !== "none" && scope.aggregationcolumn)) || isGroupByEnabled(scope.groupby) || scope.orderby;
        }

        /*Checks if the column type choosen is of number type*/
        function isNumberType(dataType) {
            var numberTypes = ['integer', 'float', 'number', 'big_decimal', 'big_integer', 'double', 'short'];
            return (numberTypes.indexOf(dataType) !== -1);
        }

        /*Gets the value by parsing upto the leaf node*/
        function getLeafNodeVal(key, dataObj) {
            var keys = key.split('.'),
                data = dataObj,
                i;
            for (i = 0; i < keys.length; i += 1) {
                if (data) {
                    data = data[keys[i]];
                } else { /*If value becomes undefined then acceess the key directly*/
                    data =  dataObj[key];
                    break;
                }
            }
            return data;
        }

        /*Charts like Line,Area,Cumulative Line does not support any other datatype
        other than integer unlike the column and bar.It is a nvd3 issue. Inorder to
        support that this is a fix*/
        function getxAxisVal(scope, dataObj, xKey, index) {
            var value = getLeafNodeVal(xKey, dataObj);
            /*If x axis is other than number type then add indexes*/
            if (isLineTypeChart(scope.type) && !isNumberType(scope.xAxisDataType)) {
                /*Verification to get the unique data keys */
                if (scope.xDataKeyArr.indexOf(value) === -1) {
                    scope.xDataKeyArr.push(value);
                }
                return index;
            }
            return value;
        }

        /*Returns the single data point based on the type of the data chart accepts*/
        function valueFinder(scope, dataObj, xKey, yKey, index, shape) {
            var xVal = getxAxisVal(scope, dataObj, xKey, index),
                value = getLeafNodeVal(yKey, dataObj),
                yVal = parseFloat(value) || value,
                dataPoint = {},
                size = parseFloat(dataObj[scope.bubblesize]) || 2,
                tooltipcolumns = scope.tooltipcolumns ? scope.tooltipcolumns.split(',') : [],
                type = scope.type;

            /*Pushing also the tooltips columns also since bubble chart need all other columns*/
            if (tooltipcolumns.length > 0 && isBubbleChart(type)) {
                tooltipcolumns.forEach(function (column) {
                    dataPoint[column] = dataObj[column];
                });
            }

            if (isChartDataJSON(type)) {
                dataPoint.x = xVal;
                dataPoint.y = yVal;
                /*only Bubble chart has the third dimension*/
                if (isBubbleChart(type)) {
                    dataPoint.size = size;
                    dataPoint.shape = shape || 'circle';
                }
            } else if (isChartDataArray(type)) {
                dataPoint = [xVal, yVal];
            }
            return dataPoint;
        }

        /*Formatting the binded data compatible to chart data*/
        function getChartData(scope) {
            var shapes = [];
            scope.sampleData = getSampleData(scope);
            /*Plotting the chart with sample data when the chart dataset is not bound*/
            if (!scope.binddataset) {
                return scope.sampleData;
            }

            if (CONSTANTS.isStudioMode) {
                scope.showContentLoadError = false;
                /** When binddataset value is there and chartData is not populated yet then a Loading message will be shown*/
                if (scope.binddataset && !scope.chartData) {
                    return [];
                }
                if (scope.isServiceVariable) {
                    scope.showContentLoadError = true;
                    scope.errMsg = $rootScope.locale.MESSAGE_INFO_SAMPLE_DATA;
                    scope.hintMsg = $rootScope.locale.MESSAGE_ERROR_DATA_DISPLAY + scope.name;
                    return scope.sampleData;
                }
                if (!scope.chartData) {
                    return scope.sampleData;
                }
            } else {
                /*When invalid axis are chosen when aggregation is enabled then plot the chart with sample data*/
                if ((!isValidAxis(scope) && isAggregationEnabled(scope))) {
                    return scope.sampleData;
                } else if (!scope.chartData) {
                    return [];
                }
            }

            var datum = [],
                xAxisKey = scope.xaxisdatakey,
                yAxisKeys = scope.yaxisdatakey ? scope.yaxisdatakey.split(',') : [],
                dataSet = scope.chartData,
                yAxisKey;

            /*check if the datasource is live variable then get the column definition else directly get the data type of the object passed*/
            if (scope.isLiveVariable) {
                scope.xAxisDataType = getColumnType(xAxisKey, scope.dataset.propertiesMap.columns);
                scope.yAxisDataType = getColumnType(yAxisKeys[0], scope.dataset.propertiesMap.columns);
            } else {
                if(scope.chartData && scope.chartData[0]) {
                    scope.xAxisDataType = getDataType(xAxisKey, scope.chartData[0]);
                    scope.yAxisDataType = getDataType(yAxisKeys[0], scope.chartData[0]);
                }
            }

            if (WM.isArray(dataSet)) {
                if (isPieType(scope.type)) {
                    yAxisKey = yAxisKeys[0];
                    datum = _.map(dataSet, function (dataObj) {
                        return valueFinder(scope, dataObj, xAxisKey, yAxisKey);
                    });
                } else {
                    if (isBubbleChart(scope.type)) {
                        shapes =  scope.shape === 'random' ? allShapes : scope.shape;
                    }
                    yAxisKeys.forEach(function (yAxisKey, series) {
                        datum.push({
                            values: _.map(dataSet, function (dataObj, index) {
                                return valueFinder(scope, dataObj, xAxisKey, yAxisKey, index, (WM.isArray(shapes) && shapes[series]) || scope.shape);
                            }),
                            key: yAxisKey
                        });
                    });
                }
            }
            return datum;
        }

        /* Getting the relevant aggregation function based on the selected option*/
        function getAggregationFunction(option) {
            switch (option) {
            case "average":
                return "AVG";
            case "count":
                return "COUNT";
            case "maximum":
                return "MAX";
            case "minimum":
                return "MIN";
            case "sum":
                return "SUM";
            default:
                return "";
            }
        }

        /*Constructing the grouped data based on the selection of orderby, x & y axis*/
        function getGroupedData(scope, queryResponse, groupingColumn) {
            var  chartData = [],
                groupData = {},
                groupValues = [],
                groupKey,
                index = 0,
                i;
            scope.xAxisDataType = getColumnType(scope.xaxisdatakey, scope.dataset.propertiesMap.columns);
            scope.yAxisDataType = getColumnType(scope.yaxisdatakey, scope.dataset.propertiesMap.columns);

            while (queryResponse.length !== 0) {
                groupKey = queryResponse[queryResponse.length - 1][groupingColumn];
                groupValues.unshift(valueFinder(scope, queryResponse[queryResponse.length - 1], scope.xaxisdatakey, scope.yaxisdatakey, 0));
                queryResponse.splice(queryResponse.length - 1, 1);
                for (i = queryResponse.length - 1; i >= 0; i -= 1) {
                    /*Checking if the new column groupKey is same as the choosen groupKey*/
                    /*Then pushing the data*/
                    /*Then splicing the data since it is already pushed*/
                    if (groupKey === queryResponse[i][groupingColumn]) {
                        index += 1;
                        groupValues.unshift(valueFinder(scope, queryResponse[i], scope.xaxisdatakey, scope.yaxisdatakey, index));
                        queryResponse.splice(i, 1);
                    }
                }

                /*Pushing the data with groupKey and values*/
                groupData = {
                    key : groupKey,
                    values : groupValues
                };
                chartData.push(groupData);
                groupValues = [];
                index = 0;
            }
            return chartData;
        }

        function getOrderbyExpression(orderby) {
            var orderbyCols = (orderby ? orderby.replace(/:/g, ' ') : '').split(','),
                trimmedCols = '';
            orderbyCols = orderbyCols.map(function (col) {
                return col.trim();
            });
            trimmedCols = orderbyCols.join();
            return trimmedCols;
        }

        /*Replacing the '.' by the '_' because '.' is not supported in the alias names*/
        function getValidAliasName(aliasName) {
            return aliasName ? aliasName.replace(/\./g, '_') : null;
        }

        /* Returns the columns that are to be fetched in the query response*/
        function getQueryColumns(scope) {
            var columns = [],
                groupbyColumns = scope.groupby ? scope.groupby.split(',') : [],
                yAxisKeys = scope.yaxisdatakey ? scope.yaxisdatakey.split(',') : [],
                expr;

            /* adding groupby columns */
            groupbyColumns.forEach(function (columnName) {
                if (columnName !== scope.aggregationcolumn) {
                    columns.push(columnName + " AS " + getValidAliasName(columnName));
                }
            });

            /* adding aggregation column, if enabled */
            if (scope.aggregation !== "none" &&  scope.aggregationcolumn) {
                columns.push(getAggregationFunction(scope.aggregation) + "(" + scope.aggregationcolumn + ") AS " + getValidAliasName(scope.aggregationcolumn));
            }

            /* adding x-axis column, if not pushed yet */
            if (scope.aggregationcolumn !== scope.xaxisdatakey) {
                expr = scope.xaxisdatakey + " AS " + getValidAliasName(scope.xaxisdatakey);
                if (columns.indexOf(expr) === -1) {
                    columns.push(expr);
                }
            }

            /* adding y-axis columns, if not pushed yet */
            yAxisKeys.forEach(function (yAxisKey) {
                if (yAxisKey !== scope.aggregationcolumn) {
                    expr = yAxisKey + " AS " + getValidAliasName(yAxisKey);
                    if (columns.indexOf(expr) === -1) {
                        columns.push(expr);
                    }
                }
            });

            return columns;
        }

        /*Decides whether the data should be visually grouped or not*/
        /*Visually grouped when a different column is choosen in the group by other than x and y axis*/
        function getGroupingDetails(scope) {
            var isVisuallyGrouped = false,
                visualGroupingColumn = '',
                groupingExpression = '',
                groupbyColumns = scope.groupby ? scope.groupby.split(',') : [],
                yAxisKeys = scope.yaxisdatakey ? scope.yaxisdatakey.split(',') : [],
                groupingColumnIndex;

            if (scope.groupby) {
                /*Getting the group by column which is not selected either in x or y axis*/
                groupbyColumns.every(function (column, index) {
                    if (scope.xaxisdatakey !== column && WM.element.inArray(column, yAxisKeys) === -1) {
                        isVisuallyGrouped = true;
                        visualGroupingColumn = column;
                        groupingColumnIndex = index;
                        groupbyColumns.splice(groupingColumnIndex, 1);
                        return false;
                    }
                    return true;
                });
                /*Constructing the groupby expression*/
                if (visualGroupingColumn) {
                    groupingExpression = visualGroupingColumn;
                    if (groupbyColumns.length) {
                        groupingExpression += ",";
                    }
                }

                if (groupbyColumns.length) {
                    groupingExpression += groupbyColumns.join();
                }
            }

            /* set isVisuallyGrouped flag in scope for later use */
            scope.isVisuallyGrouped = isVisuallyGrouped;

            return {
                expression: groupingExpression,
                isVisuallyGrouped: isVisuallyGrouped,
                visualGroupingColumn: visualGroupingColumn
            };
        }

        /*Function to get the aggregated data after applying the aggregation & group by or order by operations.*/
        function getAggregatedData(scope, element, callback) {
            var query,
                variableName,
                variable,
                columns,
                yAxisKeys = scope.yaxisdatakey ? scope.yaxisdatakey.split(',') : [],
                orderbyexpression = getOrderbyExpression(scope.orderby),
                groupingDetails = getGroupingDetails(scope),
                groupbyExpression = groupingDetails.expression,
                elScope = element.scope();

            /*Returning if the data is not yet loaded*/
            if (!scope.chartData) {
                return;
            }

            /*Set the variable name based on whether the widget is bound to a variable opr widget*/
            if (scope.binddataset.indexOf("bind:Variables.") !== -1) {
                variableName = scope.binddataset.replace("bind:Variables.", "");
                variableName = variableName.substr(0, variableName.indexOf("."));
            } else {
                variableName = scope.dataset.variableName;
            }

            variable = elScope.Variables && elScope.Variables[variableName];
            if (!variable) {
                return;
            }
            columns = getQueryColumns(scope);
            query = QueryBuilder.getQuery({
                "tableName": variable.type,
                "columns": columns,
                "filterFields": scope.filterFields || variable.filterFields,
                "groupby": groupbyExpression,
                "orderby": orderbyexpression
            });

            /*Execute the query.*/
            QueryBuilder.executeQuery({
                "databaseName": variable.liveSource,
                "query": query,
                "page": 1,
                "size": 500,
                "nativeSql": false
            }, function (response) {
                /*Transform the result into a format supported by the chart.*/
                var chartData = [],
                    aggregationAlias = getValidAliasName(scope.aggregationcolumn),
                    visualGroupingColumnAlias = groupingDetails.visualGroupingColumn ? getValidAliasName(groupingDetails.visualGroupingColumn) : '',
                    xAxisAliasKey = getValidAliasName(scope.xaxisdatakey),
                    yAxisAliasKeys = [];

                yAxisKeys.forEach(function (yAxisKey) {
                    yAxisAliasKeys.push(getValidAliasName(yAxisKey));
                });

                WM.forEach(response.content, function (data) {
                    var obj = {};
                    /* Set the response in the chartData based on "aggregationColumn", "xAxisDataKey" & "yAxisDataKey".*/
                    if (scope.aggregation !== "none") {
                        obj[scope.aggregationcolumn] = data[aggregationAlias];
                    }

                    if (visualGroupingColumnAlias) {
                        obj[groupingDetails.visualGroupingColumn] = data[visualGroupingColumnAlias];
                    }

                    obj[scope.xaxisdatakey] = data[xAxisAliasKey];
                    yAxisKeys.forEach(function (yAxisKey) {
                        yAxisAliasKeys.push(getValidAliasName(yAxisKey));
                    });

                    yAxisKeys.forEach(function (yAxisKey, index) {
                        obj[yAxisKey] = data[yAxisAliasKeys[index]];
                    });

                    chartData.push(obj);
                });

                scope.chartData = groupingDetails.isVisuallyGrouped ? getGroupedData(scope, chartData, groupingDetails.visualGroupingColumn) : chartData;

                Utils.triggerFn(callback);
            });
        }

        /* Applying the font related styles for the chart*/
        function setTextStyle(properties, id) {
            var charttext = d3.select('#wmChart' + id + ' svg').selectAll('text');
            charttext.style(properties);
        }

        function modifyLegendPosition(scope) {
            var chartId = "wmChart" + scope.$id,
                legendWrap = d3.select("#" + chartId + " .nv-legendWrap"),
                legendWrapHeight,
                legendWrapTransform,
                coordinates,
                y,
                getChartHeight = function () {
                    var chartHeight = $("#" + chartId + ">svg>.nvd3.nv-wrap")[0].getBoundingClientRect().height;
                    if (chartHeight === 0) { /*fix for IE*/
                        chartHeight = ($("#" + chartId + ">svg")[0].getBoundingClientRect().height - (legendWrapHeight + 15));
                    }
                    return chartHeight;
                },
                getAxisLabelHeight = function(axis) {
                    var axisLabel = d3.select("#"+ chartId +" .nv-"+ axis +".nv-axis .nv-axislabel")[0][0];
                    return axisLabel ? axisLabel.getBoundingClientRect().height : 0;
                };

            if (!legendWrap[0][0]) {
                return;
            }

            legendWrapHeight = legendWrap[0][0].getBoundingClientRect().height;
            legendWrapTransform = (legendWrap && legendWrap.attr("transform")) ? legendWrap.attr("transform").replace(/, /g, ",") : "";
            coordinates = /translate\(\s*([^\s,)]+)[ ,]([^\s,)]+)/.exec(legendWrapTransform);

            switch (scope.legendposition) {
            case "Top":
                y = -(legendWrapHeight + 15);
                break;
            case "Bottom":
                if (scope.offsetbottom > legendWrapHeight) {
                    y = getChartHeight() - (legendWrapHeight + 15);

                    if ((scope.type !== "Bar") && scope.xaxislabel) {
                        y = y - getAxisLabelHeight('x');
                    } else if ((scope.type === "Bar") && scope.yaxislabel) {
                        y = y - getAxisLabelHeight('y');
                    }
                }
                break;
            default:
                y = +coordinates[2];
            }

            legendWrap.attr("transform", "translate(" + coordinates[1] + ", " + y + ")");
        }

        function angle(d) {
            var a = (d.startAngle + d.endAngle) * 90 / Math.PI - 90;
            return a > 90 ? a - 180 : a;
        }

        /*This function sets maximum width for the labels that can be displayed.This will helpful when they are overlapping*/
        function setLabelsMaxWidth(scope) {
            var xTicks,
                tickWidth,
                maxLength,
                xDist,
                yDist,
                totalHeight,
                maxNoLabels,
                nthElement,
                labelsAvailableWidth,
                fontsize = parseInt(scope.fontsize, 10) || 12,
                isBarchart = isBarChart(scope.type),
                barWrapper,
                yAxisWrapper,
                svgWrapper;
            /*getting the x ticks in the chart*/
            xTicks = WM.element('#wmChart' + scope.$id + ' svg').find('g.nv-x').find('g.tick').find('text');

            /*getting the distance between the two visible ticks associated with visible text*/
            xTicks.each(function () {
                var xTick = WM.element(this),
                    xTransform,
                    tickDist;
                if (xTick.text() && xTick.css('opacity') === '1') {
                    xTransform = xTick.parent().attr('transform').split(',');
                    xDist = parseFloat(xTransform[0].substr(10));
                    yDist = parseFloat(xTransform[1] || '0');
                    if (!isBarchart && xDist > 0) {
                        tickDist = xDist;
                    } else if (yDist > 0) {
                        tickDist = yDist;
                    }
                    if (tickWidth) {
                        tickWidth = tickDist - tickWidth;
                        return false;
                    }
                    tickWidth = tickDist;
                    return true;
                }
            });

            /*In case of bar chart getting the available space for the labels to be displayed*/
            if (isBarchart) {
                barWrapper = WM.element('#wmChart' + scope.$id + ' svg>g.nv-wrap>g>g.nv-barsWrap')[0];
                yAxisWrapper = WM.element('#wmChart' + scope.$id + ' svg>g.nv-wrap>g>g.nv-y')[0];
                svgWrapper = WM.element('#wmChart' + scope.$id + ' svg')[0];
                /*getting the total height of the chart*/
                totalHeight = barWrapper ? barWrapper.getBoundingClientRect().height : 0;
                /*getting the labels available space*/
                labelsAvailableWidth = yAxisWrapper ? svgWrapper.getBoundingClientRect().width - yAxisWrapper.getBoundingClientRect().width : svgWrapper.getBoundingClientRect().width;

                /*Setting the max length for the label*/
                maxLength = Math.round(labelsAvailableWidth / fontsize);
                /*if available space for each label is less than the font-size*/
                /*then limiting the labels to be displayed*/
                if (tickWidth < fontsize) {
                    /*calculate the maximum no of labels to be fitted*/
                    maxNoLabels = totalHeight / fontsize;
                    /*showing only the nth element*/
                    nthElement = Math.ceil(scope.chartData.length / maxNoLabels);
                    /*showing up only some labels*/
                    d3.select('#wmChart' + scope.$id + ' svg').select('g.nv-x').selectAll('g.tick').select('text').each(function (text, i) {
                        /*hiding every non nth element*/
                        if (i % nthElement !== 0) {
                            d3.select(this).attr('opacity', 0);
                        }
                    });
                }
            } else {
                /*Setting the max length for the label*/
                maxLength = Math.round(tickWidth / fontsize);
            }

            /*Validating if every label exceeds the max length and if so limiting the length and adding ellipsis*/
            xTicks.each(function () {
                if (this.textContent.length > maxLength) {
                    this.textContent = this.textContent.substr(0, maxLength) + '...';
                }
            });
        }

        /* Returns the columns of that can be choosen in the x and y axis*/
        function getDefaultColumns(scope) {
            var defaultColumns = [],
                type,
                stringColumn,
                columns = scope.isLiveVariable ? scope.dataset.propertiesMap.columns : [],
                i,
                temp;


            for (i = 0; i < columns.length && defaultColumns.length <= 2; i += 1) {
                type = columns[i].type;
                if (!columns[i].isRelated && (isNumberType(type))) {
                    defaultColumns.push(columns[i].fieldName);
                } else if (type === 'string' && !stringColumn) {
                    stringColumn = columns[i].fieldName;
                }
            }
            /*Other than bubble chart x: string type y: number type*/
            /*Bubble chart x: number type y: number type*/
            if (stringColumn && defaultColumns.length > 0 && !isBubbleChart(scope.type)) {
                temp = defaultColumns[0];
                defaultColumns[0] = stringColumn;
                defaultColumns[1] = temp;
            }

            return defaultColumns;
        }

        /*Creating a formatter based on the number format and no of digits chosen*/
        function getFormatOptions(numberformat, digits) {
            var formater,
                nodigits = WM.isDefined(digits) ? digits.toString() : '';
            switch (numberformat) {
            case 'Display Digits':
                formater = nodigits + 'f';
                break;
            case 'Decimal Digits':
                formater = '.' + nodigits + 'f';
                break;
            case 'Precision':
                formater = '.' + nodigits + 'g';
                break;
            case 'Exponential':
                formater = '.' + nodigits + 'e';
                break;
            case 'Percentage':
                formater = '.' + nodigits + '%';
                break;
            case 'Round':
                formater = ',' + nodigits + 'r';
                break;
            case 'Round Percentage':
                formater = '.' + nodigits + 'p';
                break;
            }
            return formater;
        }

        /* intializes the chart obejct */
        function initChart(scope) {
            var chart,
                bgColor,
                textColor,
                theme,
                tooltipContent,
                tooltipColumns = [],
                divider;
            divider = tickformats[scope.ynumberformat] ? tickformats[scope.ynumberformat].divider : 1;
            switch (scope.type) {
            case 'Column':
                chart = nv.models.multiBarChart()
                    .x(function (d) {
                        return d.x;
                    })
                    .y(function (d) {
                        return d.y;
                    })
                    .staggerLabels(scope.staggerlabels)
                    .reduceXTicks(scope.reducexticks)
                    .rotateLabels(0)
                    .showControls(scope.showcontrols)
                    .tooltips(scope.tooltips)
                    .groupSpacing(scope.barspacing);
                break;
            case 'Cumulative Line':
                chart = nv.models.cumulativeLineChart()
                    .x(function (d) {
                        return d[0];
                    })
                    .y(function (d) {
                        return d[1] / 100;
                    })
                    .useInteractiveGuideline(true)
                    .showControls(scope.showcontrols)
                    .tooltips(scope.tooltips);
                break;
            case 'Line':
                chart = nv.models.lineChart()
                    .useInteractiveGuideline(true)
                    .tooltips(scope.tooltips);
                break;
            case 'Area':
                chart = nv.models.stackedAreaChart()
                    .x(function (d) {
                        return d[0];
                    })
                    .y(function (d) {
                        return d[1];
                    })
                    .clipEdge(true)
                    .showControls(scope.showcontrols)
                    .useInteractiveGuideline(true)
                    .tooltips(scope.tooltips);
                break;
            case 'Bar':
                chart = nv.models.multiBarHorizontalChart()
                    .x(function (d) {
                        return d.x;
                    })
                    .y(function (d) {
                        return d.y;
                    })
                    .showControls(scope.showcontrols)
                    .showValues(scope.showvalues);
                break;
            case 'Pie':
            case 'Donut':
                chart = nv.models.pieChart()
                    .x(function (d) {
                        return d.x;
                    })
                    /*Dividing the respective value with divider[1000,1000000,1000000000] based on the number format choosen*/
                    .y(function (d) {
                        return (parseFloat((d.y / divider).toFixed(2)) || d.y);
                    })
                    .tooltips(scope.tooltips)
                    .showLabels(scope.showlabels)
                    .labelType(scope.labeltype)
                    .labelThreshold(0.04);
                if (isDonutChart(scope.type)) {
                    chart.donut(true)
                        .donutRatio(scope.donutratio)
                        .donutLabelsOutside(scope.showlabelsoutside);
                } else {
                    chart.pieLabelsOutside(scope.showlabelsoutside);
                }
                break;
            case 'Bubble':
                chart = nv.models.scatterChart()
                    .x(function (d) {
                        return d.x;
                    })
                    .y(function (d) {
                        return d.y;
                    })
                    .tooltips(scope.tooltips)
                    .showDistX(scope.showxdistance)
                    .showDistY(scope.showydistance);
                break;
            }

            if (chartTypes.indexOf(scope.type) === -1) {
                chart = nv.models.multiBarChart()
                    .x(function (d) {
                        return d.x;
                    })
                    .y(function (d) {
                        return d.y;
                    })
                    .tooltips(true);
            }

            chart.showLegend(scope.showlegend)
                .margin({top: scope.offsettop, right: scope.offsetright, bottom: scope.offsetbottom, left: scope.offsetleft})
                .color(themes[scope.theme].colors);
            /*setting the no data message*/
            chart.noData(scope.message);
            theme = scope.theme;
            bgColor = themes[theme].tooltip.backgroundColor;
            textColor = themes[theme].tooltip.textColor;
            if (!isPieType(scope.type) && !isBubbleChart(scope.type)) {
                chart.tooltipContent(function (key, x, y) {
                    return "<div style='text-align: center;'><p style='background-color:" + bgColor + ";color:" + textColor + "'><strong>" + key + "</strong><br></p>" + x + " on " + y + "</div>";
                });
            } else if (isBubbleChart(scope.type)) {
                /*By default bubble chart doesn't provide any tooltips,so constructing the tooltips*/
                tooltipColumns = scope.tooltipcolumns ? scope.tooltipcolumns.split(',') : [];
                chart.tooltipContent(function (key, x, y, data, dataPoint) {
                    if (tooltipColumns) {
                        tooltipContent = "<div class='tooltip-container'>";
                        tooltipColumns.forEach(function (column) {
                            tooltipContent += '<h5><strong>' + column + "</strong> : " + dataPoint.point[column] + '</h5>';
                        });
                        tooltipContent += '</div>';
                    }
                    return tooltipContent;
                });
            } else {
                chart.tooltipContent(function (key, x) {
                    return "<div style='text-align: center'><p style='background-color:" + bgColor + ";color:" + textColor + "'><strong>" + key + "</strong><br></p>" + x + "</div>";
                });
            }
            return chart;
        }

        function postPlotProcess(scope, element, chart) {
            var chartSvg,
                pieLabels,
                pieGroups,
                angleArray,
                styleObj = {};

            if (!isPieType(scope.type)) {
                setLabelsMaxWidth(scope);
            } else if (!scope.showlabelsoutside) {
                /** Nvd3 has a issue in rotating text. So we will use this as a temp fix.
                 * If the issue is resolved there, we can remove this.*/
                /* If it is a donut chart, then rotate the text and position them*/
                chartSvg = d3.select('#wmChart' + scope.$id + ' svg');
                pieLabels = chartSvg.select('.nv-pieLabels').selectAll('.nv-label');
                pieGroups = chartSvg.select('.nv-pie').selectAll('.nv-slice');
                angleArray = [];
                if (pieGroups && pieGroups.length) {
                    pieGroups.each(function () {
                        d3.select(this).attr('transform', function (d) {
                            angleArray.push(angle(d));
                        });
                    });
                    pieLabels.each(function (d, i) {
                        var group = d3.select(this);
                        WM.element(group[0][0]).find("text").attr('transform', 'rotate(' + angleArray[i] + ')');
                    });
                }
            }

            /* prepare text style props object and set */
            WM.forEach(styleProps, function (value, key) {
                if (key === 'fontsize' || key === 'fontunit') {
                    styleObj[value] = scope.fontsize + scope.fontunit;
                } else {
                    styleObj[value] = scope[key];
                }
            });
            setTextStyle(styleObj, scope.$id);
            /*Modifying the legend position only when legend is shown*/
            if (scope.showlegend) {
                modifyLegendPosition(scope);
            }

            /*
             * allow window-resize functionality, for only-run mode as
             * updating chart is being handled by watchers of height & width in studio-mode
             * */
            if (CONSTANTS.isRunMode) {
                nv.utils.windowResize(function () {
                    if (element[0].getBoundingClientRect().height) {
                        chart.update();
                        if (!isPieType(scope.type)) {
                            setLabelsMaxWidth(scope);
                        }
                    } else {
                        var parent = element.closest('.app-accordion-panel, .tab-pane').isolateScope();
                        if (parent) {
                            parent.initialized = false;
                        }
                    }
                });
            }
        }

        /* prepares and configures the chart properties */
        function configureChart(scope, element, datum) {
            /* checking the parent container before plotting the chart */
            if (!element[0].getBoundingClientRect().height) {
                return;
            }
            var chart,
                xFormat,
                yFormat,
                xnumberformat = scope.xnumberformat,
                ynumberformat = scope.ynumberformat,
                xaxislabel,
                yaxislabel;

            if (scope.xnumberformat && scope.xdigits) {
                xFormat = getFormatOptions(scope.xnumberformat, scope.xdigits);
            }
            if (scope.ynumberformat && scope.ydigits) {
                yFormat = getFormatOptions(scope.ynumberformat, scope.ydigits);
            } else if (scope.type === "Cumulative Line") {
                yFormat = getFormatOptions('Percentage', '2');
            }

            /*empty svg to add-new chart*/
            element.find('svg').empty();

            /* get the chart obejct */
            chart = initChart(scope);

            if (!isPieType(scope.type)) {
                /*Setting the labels if they are specified explicitly or taking the axiskeys chosen*/
                xaxislabel = scope.xaxislabel || scope.xaxisdatakey || 'x caption';
                yaxislabel = scope.yaxislabel || scope.yaxisdatakey || 'y caption';
                /*Adding the units to the captions if they are specified*/
                xaxislabel += scope.xunits ? "(" + scope.xunits + ")" : "";
                yaxislabel += scope.yunits ? "(" + scope.yunits + ")" : "";
                chart.xAxis
                    .axisLabel(xaxislabel)
                    .axisLabelDistance(scope.xaxislabeldistance)
                    .tickFormat(function (d) {
                        return formatData(scope, d, scope.xAxisDataType, {dateFormat: scope.xdateformat, numberFormat: xnumberformat, format: xFormat, isXaxis: true, xDataKeyArr: scope.xDataKeyArr});
                    });
                chart.yAxis
                    .axisLabel(yaxislabel)
                    .axisLabelDistance(scope.yaxislabeldistance)
                    .tickFormat(function (d) {
                        return formatData(scope, d, scope.yAxisDataType, {dateFormat: scope.ydateformat, numberFormat: ynumberformat, format: yFormat, isXaxis: false, xDataKeyArr: scope.xDataKeyArr});
                    });
            } else {
                /*In case of pie/donut chart formatting the values of it*/
                chart.valueFormat(function (d) {
                    return formatData(scope, d, scope.yAxisDataType, {dateFormat: scope.ydateformat, numberFormat: ynumberformat, format: yFormat, isXaxis: false, xDataKeyArr: scope.xDataKeyArr});
                });
            }


            /** changing the default no data message**/
            d3.select('#wmChart' + scope.$id + ' svg')
                .datum(datum)
                .call(chart);

            postPlotProcess(scope, element, chart);
            return chart;
        }

        /* Plotting the chart with set of the properties set to it*/
        function plotChart(scope, element) {
            var datum;
            /*Plot the chart only if valid axis are chosen and aggregation not enabled.
             When aggregation enabled, in run time plot chart with sample data so not returning in that case*/
            if (!isValidAxis(scope)) {
                if (!(CONSTANTS.isRunMode && isAggregationEnabled(scope))) {
                    return;
                }
            }

            /*call user-transformed function*/
            scope.chartData = (scope.onTransform && scope.onTransform({$scope: scope})) || scope.chartData;

            /*Getting the order by data only in run mode. The order by applies for all the charts other than pie and donut charts*/
            if (scope.isVisuallyGrouped && !isPieType(scope.type)) {
                datum = scope.chartData;
            } else {
                datum = getChartData(scope);
            }

            /*return if datum is empty*/
            if ((scope.variableInflight === false && datum && datum.length === 0)) {
                scope.message = scope.nodatamessage || 'No data found';
            }

            nv.addGraph(function () {
                configureChart(scope, element, datum);
            });
        }

        function plotChartProxy(scope, element) {
            /*If aggregation/group by/order by properties have been set, then get the aggregated data and plot the result in the chart.*/
            if (scope.binddataset && scope.isLiveVariable && (scope.filterFields || isAggregationEnabled(scope))) {
                getAggregatedData(scope, element, function () {
                    plotChart(scope, element);
                });
            } else { /*Else, simply plot the chart.*/
                /*In case of live variable resetting the aggregated data to the normal dataset when the aggregation has been removed*/
                if (scope.dataset && scope.dataset.data && scope.isLiveVariable) {
                    scope.chartData = scope.dataset.data;
                }
                plotChart(scope, element);
            }
        }

        /* sets the default x and y axis options */
        function setDefaultAxisOptions(scope) {
            var defaultColumns = getDefaultColumns(scope);
            /*If we get the valid default columns then assign them as the x and y axis*/
            /*In case of service variable we may not get the valid columns because we cannot know the datatypes*/
            scope.xaxisdatakey = defaultColumns[0] || null;
            scope.yaxisdatakey = defaultColumns[1] || null;
            scope.$root.$emit("set-markup-attr", scope.widgetid, {'xaxisdatakey': scope.xaxisdatakey, 'yaxisdatakey': scope.yaxisdatakey});
        }

        /*Function that iterates through all the columns and then fetching the numeric and non primary columns among them*/
        function setNumericandNonPrimaryColumns(scope) {
            var columns,
                type;
            scope.numericColumns = [];
            scope.nonPrimaryColumns = [];
            /*Fetching all the columns*/
            if (scope.dataset && scope.dataset.propertiesMap) {
                columns = Utils.fetchPropertiesMapColumns(scope.dataset.propertiesMap);
            }

            if (columns) {
                /*Iterating through all the columns and fetching the numeric and non primary key columns*/
                WM.forEach(Object.keys(columns), function (key) {
                    type = columns[key].type;
                    if (isNumberType(type)) {
                        scope.numericColumns.push(key);
                    }
                    if (!columns[key].isPrimaryKey) {
                        scope.nonPrimaryColumns.push(key);
                    }
                });
            }
        }

        /*Sets the aggregation columns*/
        function setAggregationColumns(scope) {
            /*Set the "aggregationColumn" to show all keys in case of aggregation function is count or to numeric keys in all other cases.*/
            scope.widgetProps.aggregationcolumn.options = scope.aggregation !== "count" ? scope.numericColumns : scope.axisoptions;
        }

        /*Sets the groupby columns to the non primary key columns and other than aggregation column if chosen*/
        function setGroupByColumns(scope) {
            var index,
                columns = WM.copy(scope.nonPrimaryColumns),
                choosenColumn = scope.widgetProps.groupby && scope.widgetProps.groupby.selectedvalues ?  scope.widgetProps.groupby.selectedvalues.split(',')[0] : '';
            /*Removing the aggregation column out of the non primary columns*/
            if (scope.nonPrimaryColumns && scope.aggregationcolumn) {
                index = scope.nonPrimaryColumns.indexOf(scope.aggregationcolumn);
                if (index >= 0) {
                    columns.splice(index, 1);
                }
            }
            /*Making groupby as single select when chart is of pie type*/
            if (isPieType(scope.type)) {
                scope.widgetProps.groupby.widget = 'list';
                /*Adding the none option to the groupby columns*/
                if (columns && columns.length > 0 && columns.indexOf("none") === -1) {
                    columns.push('none');
                }
                scope.widgetProps.groupby.options = columns;
                $rootScope.$emit('update-widget-property', 'groupby', choosenColumn);
            } else {
                scope.widgetProps.groupby.widget = 'multiselect';
                scope.widgetDataset['groupby']  = columns ? columns.join(',') : '';
            }
        }

        /* Define the property change handler. This function will be triggered when there is a change in the widget property */
        function propertyChangeHandler(scope, element, key, newVal, oldVal) {
            switch (key) {
            case "dataset":
                var variableName,
                    variableObj,
                    elScope = element.scope();
                /*Set the variable name based on whether the widget is bound to a variable opr widget*/
                if (scope.binddataset && scope.binddataset.indexOf("bind:Variables.") !== -1) {
                    variableName = scope.binddataset.replace("bind:Variables.", "");
                    variableName = variableName.substr(0, variableName.indexOf("."));
                } else {
                    variableName = scope.dataset.variableName;
                }
                /*Resetting the flag to false when the binding was removed*/
                if (!newVal && !scope.binddataset) {
                    scope.isVisuallyGrouped = false;
                }

                variableObj = elScope.Variables && elScope.Variables[variableName];
                /*setting the flag for the live variable in the scope for the checks*/
                scope.isLiveVariable = variableObj && variableObj.category === 'wm.LiveVariable';
                /*If binded to a live variable feed options to the aggregation and group by*/
                if (scope.isLiveVariable && CONSTANTS.isStudioMode) {
                    /*Updating the numeric and non primary columns when dataset is changed*/
                    setNumericandNonPrimaryColumns(scope);
                    setAggregationColumns(scope);
                    setGroupByColumns(scope);
                }
                scope.isServiceVariable = variableObj && variableObj.category === 'wm.ServiceVariable';

                /*liveVariables contain data in 'data' property" of the variable*/
                scope.chartData = scope.isLiveVariable ? newVal && (newVal.data || '') : newVal;

                /*if the data returned is an object make it an array of object*/
                if (!WM.isArray(scope.chartData) && WM.isObject(scope.chartData)) {
                    scope.chartData = [scope.chartData];
                }
                scope.axisoptions = WidgetUtilService.extractDataSetFields(scope.dataset, scope.dataset.propertiesMap);

                /* scope variables used to keep the actual key values for x-axis */
                scope.xDataKeyArr = [];
                /* perform studio mode actions */
                if (CONSTANTS.isStudioMode) {
                    /* if dataset changed from workspace controller, set default columns */
                    if (scope.newcolumns) {
                        setDefaultAxisOptions(scope);
                        scope.newcolumns = false;
                    }
                    /*hiding the aggregation,group by and order by upon binding to the service variable*/
                    hideOrShowProperties(advanceDataProps, scope, scope.isLiveVariable);
                    modifyAxesOptions(scope);
                }

                if (newVal.filterFields) {
                    scope.filterFields = newVal.filterFields;
                }

                /* plotchart for only valid data */
                if (scope.chartData.length) {
                    plotChartProxy(scope, element);
                }
                break;
            case "xaxisdatakey":
                if (scope.chartReady) {
                    /*Showing the formatting options for x axis based on the type of it*/
                    if (CONSTANTS.isStudioMode) {
                        displayFormatOptions(scope, 'x');
                    }
                    plotChartProxy(scope, element);
                }
                break;
            case "yaxisdatakey":
                if (scope.chartReady) {
                    /*Showing the formatting options for y axis based on the type of it*/
                    if (CONSTANTS.isStudioMode) {
                        displayFormatOptions(scope, 'y');
                    }
                    plotChartProxy(scope, element);
                }
                break;
            case "type":
                /*setting group by columns based on the chart type*/
                setGroupByColumns(scope);
                /*Based on the change in type deciding the default margins*/
                if (isPieType(newVal)) {
                    scope.offsettop = 0;
                    scope.offsetright = 0;
                    scope.offsetbottom = 0;
                    scope.offsetleft = 0;
                } else if(isPieType(oldVal)) {
                    scope.offsettop = 25;
                    scope.offsetright = 25;
                    scope.offsetbottom = 55;
                    scope.offsetleft = 75;
                }

                /* In studio mode, configure properties dependent on chart type */
                if (CONSTANTS.isStudioMode) {
                    togglePropertiesByChartType(scope);
                }

                if (scope.chartReady) {
                    plotChartProxy(scope, element);
                }
                break;
            case "height":
            case "width":
            case "show":
            case "xaxislabel":
            case "yaxislabel":
            case "xunits":
            case "yunits":
            case "xnumberformat":
            case "xdigits":
            case "xdateformat":
            case "ynumberformat":
            case "ydigits":
            case "ydateformat":
            case "showlegend":
            case "showvalues":
            case "showlabels":
            case "showcontrols":
            case "staggerlabels":
            case "reducexticks":
            case "offsettop":
            case "offsetbottom":
            case "offsetright":
            case "offsetleft":
            case "barspacing":
            case "xaxislabeldistance":
            case "yaxislabeldistance":
            case "theme":
            case "labeltype":
            case "donutratio":
            case "showlabelsoutside":
            case "showxdistance":
            case "showydistance":
            case "bubblesize":
            case "shape":
                /**In RunMode, the plotchart method will not be called for all property change */
                if (scope.chartReady) {
                    plotChartProxy(scope, element);
                }
                break;
            case "aggregation":
                /*In case of studio mode setting the aggregation columns*/
                if (CONSTANTS.isStudioMode) {
                    toggleAggregationColumnState(scope);
                    modifyAxesOptions(scope);
                    /*Plot the chart when a valid aggregation function and column are chosen*/
                    if (scope.aggregation !== "none") {
                        /*Setting the aggregation columns based on the aggregation function chosen*/
                        setAggregationColumns(scope);
                    }
                }
                /*In case of run mode plotting the chart if valid columns are chosen*/
                if (scope.aggregation !== "none" && scope.aggregationcolumn && scope.groupby) {
                    plotChartProxy(scope, element);
                }
                break;
            case "aggregationcolumn":
                /*In case of studio mode setting the x,y,group by columns*/
                if (scope.chartReady) {
                    if (CONSTANTS.isStudioMode) {
                        modifyAxesOptions(scope);
                        /*Setting the group by columns when aggregation column is changed*/
                        setGroupByColumns(scope);
                    }
                    /*Plot the chart when a valid aggregation column are chosen*/
                    if (scope.aggregation !== "none" && scope.groupby) {
                        plotChartProxy(scope, element);
                    }
                }
                break;
            case "groupby":
                /*In case of studio mode setting the x,y columns*/
                if (scope.chartReady) {
                    if (CONSTANTS.isStudioMode) {
                        toggleAggregationState(scope);
                        scope.widgetProps.groupby.selectedvalues = newVal;
                        modifyAxesOptions(scope);
                    }
                    /*Re-plot the chart when the group by columns are chosen*/
                    plotChartProxy(scope, element);
                }
                break;
            case "orderby":
                /*Re-plot the chart when the order by columns are chosen*/
                if (scope.chartReady) {
                    plotChartProxy(scope, element);
                }
                break;
            case "fontsize":
            case "fontunit":
            case "color":
            case "fontfamily":
            case "fontweight":
            case "fontstyle":
            case "textdecoration":
                if (scope.chartReady) {
                    var styleObj = {};
                    styleObj[styleProps[key]] = (key === 'fontsize' || key === 'fontunit') ? scope.fontsize + scope.fontunit : newVal;
                    setTextStyle(styleObj, scope.$id);
                }
                break;
            case "legendposition":
                /*Modifying the legend position only when legend is shown*/
                if (scope.showlegend) {
                    modifyLegendPosition(scope);
                }
                break;
            case "nodatamessage":
                plotChartProxy(scope, element);
                break;
            }
        }

        var notifyFor = {
            'dataset': true,
            'xaxisdatakey': true,
            'yaxisdatakey': true,
            'type': true,
            'height': true,
            'width': true,
            'show': true,
            'xaxislabel': true,
            'yaxislabel': true,
            'xunits': true,
            'yunits': true,
            'xnumberformat': true,
            'xdigits': true,
            'xdateformat': true,
            'ynumberformat': true,
            'ydigits': true,
            'ydateformat': true,
            'showlegend': true,
            'showvalues': true,
            'showlabels': true,
            'showcontrols': true,
            'staggerlabels': true,
            'reducexticks': true,
            'offsettop': true,
            'offsetbottom': true,
            'offsetright': true,
            'offsetleft': true,
            'barspacing': true,
            'xaxislabeldistance': true,
            'yaxislabeldistance': true,
            'theme': true,
            'labeltype': true,
            'donutratio': true,
            'showlabelsoutside': true,
            'aggregation': true,
            'aggregationcolumn': true,
            'groupby': true,
            'orderby': true,
            'fontsize': true,
            'fontunit': true,
            'color': true,
            'fontfamily': true,
            'fontweight': true,
            'fontstyle': true,
            'textdecoration': true,
            'legendposition': true,
            'shape': true,
            'nodatamessage': true
        };


        return {
            restrict: 'E',
            replace: true,
            scope: {
                "scopedataset": '=?',
                "onTransform": '&'
            },
            template: $templateCache.get("template/widget/form/chart.html"),
            compile: function () {
                return {
                    pre: function (scope) {
                        /*Binding widget properties obtained from PropertiesFactory to scope*/
                        scope.widgetProps = WM.copy(widgetProps);
                    },
                    post: function (scope, element, attrs) {
                        var handlers = [],
                            boundVariableName;
                        /* flag to prevent initial chart plotting on each property change */
                        scope.chartReady = false;

                        /*add id the the chart*/
                        element.attr('id', 'wmChart' + scope.$id);
                        scope.widgetDataset = {};

                        /* register the property change handler */
                        WidgetUtilService.registerPropertyChangeListener(propertyChangeHandler.bind(undefined, scope, element), scope, notifyFor);

                        /*Executing WidgetUtilService method to initialize the widget with the essential configurations.*/
                        WidgetUtilService.postWidgetCreate(scope, element, attrs);

                        /** Note:  The below code has to be called only after postWidgetCreate */
                        /** During initial load the plot chart will be called only once. During load time, "plotChart" should not
                         * be called on each property change*/
                        scope.chartReady = true;

                        /* When there is not value binding, then plot the chart with sample data */
                        if (!scope.binddataset) {
                            plotChartProxy(scope, element);
                        }

                        /* Run Mode Iniitilzation */
                        if (CONSTANTS.isRunMode) {
                            /* fields defined in scope: {} MUST be watched explicitly */
                            /*watching scopedataset attribute to plot chart for the element.*/
                            scope.$watch("scopedataset", function (newVal) {
                                scope.chartData = newVal || scope.chartData;
                                plotChartProxy(scope, element);
                            });
                        } else {
                            /* on canvas-resize, plot the chart again */
                            scope.$on('$destroy', scope.$root.$on('canvas-resize', function () {
                                plotChartProxy(scope, element);
                            }));
                        }

                        if (scope.binddataset && scope.binddataset.indexOf("bind:Variables.") !== -1) {
                            boundVariableName = scope.binddataset.replace("bind:Variables.", "");
                            boundVariableName = boundVariableName.split('.')[0];
                            handlers.push($rootScope.$on('toggle-variable-state', function (event, variableName, active, response) {
                                /*based on the active state and response toggling the 'loading data...' and 'no data found' messages */
                                /*variable is active.so showing loading data message*/
                                if(boundVariableName === variableName) {
                                    scope.variableInflight = active;
                                    scope.message = active ? 'Loading Data...' : '';
                                    plotChart(scope, element);
                                }
                            }));
                        }

                        /*Container widgets like tabs, accordions will trigger this method to redraw the chart.*/
                        scope.redraw = plotChartProxy.bind(undefined, scope, element);
                    }
                };
            }
        };
    });

/**
 * @ngdoc directive
 * @name wm.widgets.basic.directive:wmChart
 * @restrict E
 *
 * @description
 * The `wmChart` directive defines a chart widget.
 *
 * @scope
 *
 * @requires PropertiesFactory
 * @requires $rootScope
 * @requires $templateCache
 * @requires WidgetUtilService
 *
 * @param {string=} name
 *                  Name of the chart widget.
 * @param {list=} type
 *                  The type of the chart.
 * @param {string=} width
 *                  Width of the chart.
 * @param {string=} height
 *                  Height of the chart.
 * @param {string=} offset
 *                  This property controls the offset of the chart.
 * @param {string=} scopedatavalue
 *                  Variable defined in controller scope.<br>
 *                  The value of this variable is used as data in plotting chart.
 * @param {string=} dataset
 *                  Sets the data for the chart.<br>
 *                  This property supports binding with variables.<br>
 *                  When bound to a variable, the data associated with the variable becomes the basis for data for plotting the chart.
 * @param {list=} aggregation
 *                  Shows the options to aggregate the data in the chart.<br>
 * @param {list=} aggregationcolumn
 *                  Shows the options to aggregate the data in the chart.<br>
 * @param {list=} groupby
 *                  Shows the options to group the data.<br>
 * @param {list=} xaxisdatakey
 *                  The key of the object, i.e x-axis variable, on the chart.<br>
 * @param {string=} xaxislabel
 *                  The caption of x axis on the chart.<br>
 * @param {list=} xnumberformat
 *                  Shows the options to format the number type in x axis.<br>
 * @param {number=} xdigits
 *                  The number of digits to be displayed after decimal in x axis.<br>
 * @param {list=} xdateformat
 *                  Shows the options to format the date type in x axis.<br>
 * @param {number=} xaxislabeldistance
 *                  This property controls the distance between the x axis and its label.<br>
 * @param {list=} yaxisdatakey
 *                  The key of the object, i.e y-axis variable, on the chart.<br>
 * @param {string=} yaxislabel
 *                  The caption of x axis on the chart.<br>
 * @param {list=} ynumberformat
 *                  Shows the options to format the number type in x axis.<br>
 * @param {number=} ydigits
 *                  The number of digits to be displayed after decimal in x axis.<br>
 * @param {list=} ydateformat
 *                  Shows the options to format the date type in x axis.<br>
 * @param {number=} yaxislabeldistance
 *                  This property controls the distance between the x axis and its label.<br>
 * @param {boolean=} show
 *                  Show is a bindable property. <br>
 *                  This property will be used to show/hide the chart widget on the web page. <br>
 *                  Default value: `true`. <br>
 *@param {boolean=} tooltips
 *                  This property controls whether to show the tooltip on hover. <br>
 *@param {boolean=} showlegend
 *                  This property controls whether to show the legends. <br>
 *@param {boolean=} showvalues
 *                  This property controls showing of values on the bars. <br>
 *@param {boolean=} showlabels
 *                  This property controls showing of labels. <br>
 *@param {boolean=} showcontrols
 *                  This property controls showing the default controls for charts. <br>
 *@param {boolean=} staggerlabels
 *                  This property controls whether to stagger the labels which distributes labels into multiple lines. <br>
 *@param {boolean=} reducexticks
 *                  This property controls whether to reduce the xticks or not. <br>
 *@param {list=} labeltype
 *                  This property controls the type of the label to be shown in the chart. <br>
 *                  Key is the value of the key data, value is the data value, and percent represents the percentage that the slice of data represents. <br>
 *@param {number=} barspacing
 *                  This property controls the spacing between the bars and value ranges from 0.1 to 0.9. <br>
 *@param {number=} donutratio
 *                  This property controls the radius and value ranges from 0.1 to 1. <br>
 *@param {boolean=} showlabelsoutside
 *                  This property controls the labels should be outside or inside. <br>
 * @param {string=} on-transform
 *                  Callback function for `transform` event.
 *
 *
 *
 *
 *
 *
 * @example
 *   <example module="wmCore">
 *       <file name="index.html">
 *           <div data-ng-controller="Ctrl" class="wm-app">
 *              <wm-chart
 *                  type="Column"
 *                  name="chart1"
 *                  tooltips="false"
 *                  staggerlabels="true"
 *                  barspacing="0.2">
 *              </wm-chart>
 *           </div>
 *       </file>
 *       <file name="script.js">
 *          function Ctrl($scope) {
 *           }
 *       </file>
 *   </example>
 */
