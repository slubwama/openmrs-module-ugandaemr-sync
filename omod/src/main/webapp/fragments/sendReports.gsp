<%
    def breadcrumbMiddle = breadcrumbOverride ?: '';
%>
<style type="text/css">
#blurred
{
    display : none;
}
#blurred.show
{
    display : block;
    position : fixed;
    z-index: 100;
    background-image : url('http://loadinggif.com/images/image-selection/3.gif');
    background-color:#666;
    opacity : 0.4;
    background-repeat : no-repeat;
    background-position : center;
    left : 0;
    bottom : 0;
    right : 0;
    top : 0;
}

#loader {
    position: absolute;
    top: 50%;
    left: 50%;
    margin: -50px 0px 0px -50px;
}
.grey {
    background-color:#c7c5c5 !important;
}

/* Tooltip container */
.tooltips {
    position: relative;
    display: inline-block;
}

/* Tooltip text */
.tooltips .tooltiptext {
    visibility: hidden;
    left: 0;
    top: 110%;
    width: 200px;
    background-color:#c7c5c5;
    color: #000000;
    text-align: center;
    padding: 5px 0;
    border-radius: 6px;
    text-wrap: none !important;
    position: absolute;
    z-index: 1
}

/* Show the tooltip text when you mouse over the tooltip container */
.tooltips:hover .tooltiptext {
    visibility: visible;
}
</style>
<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;}
.tg td{border-color:black;border-style:solid;border-width:1px;font-family:Arial, sans-serif;font-size:14px;
    overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{border-color:black;border-style:solid;border-width:1px;font-family:Arial, sans-serif;font-size:14px;
    font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-wdya{background-color:#D9D9D9;color:#7030A0;text-align:center;vertical-align:middle}
.tg .tg-qnnl{background-color:#F8FFF9;text-align:center;vertical-align:top}
.tg .tg-8y2u{background-color:#F8FFF9;text-align:right;vertical-align:top}
.tg .tg-o4ah{background-color:#D9D9D9;text-align:center;vertical-align:middle}
.tg .tg-ogju{background-color:#E6F1FF;text-align:center;vertical-align:top}
.tg .tg-9j11{background-color:#D9D9D9;text-align:left;vertical-align:middle}
.tg .tg-0lax{text-align:left;vertical-align:top}
.tg .tg-zlip{background-color:#F8FFF9;text-align:left;vertical-align:top}
.tg .tg-wboj{background-color:#E6F1FF;text-align:right;vertical-align:top}
</style>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.app.systemAdministration.label")}", link: '/' + OPENMRS_CONTEXT_PATH + '/coreapps/systemadministration/systemAdministration.page'},
        { label: "UgandaEMR Sync", link: '/' + OPENMRS_CONTEXT_PATH + '/ugandaemrsync/ugandaemrsync.page'},
        { label: "Send Reports"}
    ];

    var previewBody;
    var uuid;
    var merUuids;
    var hmisUuids;

    function clearDisplayReport(){
        jq("#display-report").empty();
        jq('#submit-button').empty();
    }

    function displaySurgeReport(report){
        jq("div#surge-report").show();

        jq.each(report.group, function (index, rowValue) {
            var indicatorCode = rowValue.code.coding[0].code;
            if(rowValue.stratifier.length!==0){
                var disaggregated_rows = rowValue.stratifier[0].stratum;
                var which_sex='';
                var which_age='';
                var test_result='';
                jq.each(disaggregated_rows, function (key, obj) {
                    jq.each(obj.component,function(k,v){
                        if(v.code.coding[0].code=='SEX'){
                            which_sex = v.value.coding[0].code;
                        }else if (v.code.coding[0].code=='AGE_GROUP'){
                            which_age = v.value.coding[0].code;
                        }else if (v.code.coding[0].code=='TEST_RESULT'){
                            test_result = v.value.coding[0].code;
                        }
                    });
                    // console.log('#'+indicatorCode+'-'+which_age+'-'+which_sex+'-'+test_result);
                    if(which_sex!=='' && which_age!=='' && test_result!==''){
                        if(test_result==='HIV Negative'){
                           jq('#'+indicatorCode+'-'+which_age+'-'+which_sex+'-N').html(obj.measureScore.value);
                        }else if(test_result==='HIV Positive 1st Test'){
                           jq('#'+indicatorCode+'-'+which_age+'-'+which_sex+'-P1').html(obj.measureScore.value);
                        }else if(test_result==='HIV Positive repeat Test'){
                             jq('#'+indicatorCode+'-'+which_age+'-'+which_sex+'-P2').html(obj.measureScore.value);
                        }

                    } else if(which_sex!='' && which_age!=''){
                        jq('#'+indicatorCode+'-'+which_age+'-'+which_sex).html(obj.measureScore.value);
                    }else if (which_sex=='') {
                        jq('#'+indicatorCode+'-'+which_age).html(obj.measureScore.value);
                    }


                });
            }
        });
        jq('#submit-button').show();
    }
    function displayMERReport(report){
        var reportDataString="";
        var tableHeader = "<table><thead><tr><th>Indicator Code</th><th>Indicator Name and Disaggregations </th>";
        var endTableHeader ="</thead><tbody>";
        var tableFooter = "</tbody></table>";
        var ageHeaders="";
        var sexHeaders="";
        var total_Value ="";
        var dataElementsNo;
        var firstSexPosition="";

        jq.each(report.group, function (index, rowValue) {
            var indicatorCode="";
            var total_Display_Name="";
            var dataValueToDisplay = "";
            dataValueToDisplay += "<tr>";

                indicatorCode = rowValue.code.coding[0].code;
                var indicatorDisplay = rowValue.code.coding[0].display;
                total_Value = rowValue.measureScore.value;
                // total_Display_Name = rowValue.stratifier[0].code[0].coding[0].display;



            if(rowValue.stratifier.length!==0) {
                var disaggregated_rows = rowValue.stratifier[0].stratum;
                if(disaggregated_rows.length>1){
                    var rowspanAttribute="rowspan= \""+((disaggregated_rows.length)/2)+"\"";
                }

                dataValueToDisplay += "<td "+ rowspanAttribute+ ">"+indicatorCode+ "<span>"+indicatorDisplay+"</span>"+"</td>";
                jq.each(disaggregated_rows, function (key, obj) {
                    var row_displaySexKey = "";
                    var row_displaySexName = "";
                    var row_displayDisaggregateName = "";
                    jq.each(obj.component,function(k,v){
                       if(v.code.coding[0].code=='SEX'){
                           row_displaySexKey = v.value.coding[0].code;
                           row_displaySexName = v.value.coding[0].display;
                       }else{
                           row_displayDisaggregateName = v.value.coding[0].display;
                       }
                    });

                    var row_displayValue = obj.measureScore.value;

                    var row ="";

                    if (index === 0 && key===0) {
                        firstSexPosition = row_displaySexKey;
                    }
                    if (index === 0 && (key===1 || key===0)){
                            sexHeaders = sexHeaders + "<th>" + row_displaySexName + "</th>";
                    }
                    if(key%2===0 && key!==0){
                        row+="<tr>";
                    }
                    if(row_displaySexKey === firstSexPosition){
                        row+= "<td>" + row_displayDisaggregateName + "</td><td>"+row_displayValue+"</td>";
                    }else{
                        row+= "<td>" + row_displayValue + "</td></tr>";
                    }
                    dataValueToDisplay += row;
                });
            }else{
                dataValueToDisplay+="</tr>";
            }


            reportDataString += dataValueToDisplay;
        });

        tableHeader+=sexHeaders;

        jq("#display-report").append(tableHeader+endTableHeader + reportDataString + tableFooter);
        jq('#submit-button').show();
    }

    function displayHMISReport(report){
        var reportDataString="";
        var tableHeader = "<table><thead><tr rowspan='2'><th>Data element</th>";
        var sexRow = "<tr><th></th>";
        var endTableHeader ="</thead><tbody>";
        var tableFooter = "</tbody></table>";
        var ageHeaders="";
        var sexHeaders="";
        var total_Value ="";
        var dataElementsNo;

        jq.each(report.group, function (index, rowValue) {
            var indicatorCode="";
            var total_Display_Name="";
            var dataValueToDisplay = "";
            dataValueToDisplay += "<tr>";



            indicatorCode = rowValue.code.coding[0].code;
            var indicatorDisplay = rowValue.code.coding[0].display;
            var row_display_third_diaggregate ="";
            var row_data_values ="";
            // total_Display_Name = rowValue.stratifier[0].code[0].coding[0].display;
             total_Value = rowValue.measureScore.value;

            if(rowValue.stratifier.length!==0) {
                var disaggregated_rows = rowValue.stratifier[0].stratum;

                jq.each(disaggregated_rows, function (key, obj) {
                    var row_displayAgeKey = obj.component[0].value.coding[0].code;
                    var row_displaySexKey = obj.component[1].value.coding[0].code;
                    var row_displayValue = obj.measureScore.value;
                    var row_displayAgeName = obj.component[0].value.coding[0].display;

                    if (index == 0) {
                        if (disaggregated_rows.length >=120 && key >=24) {}
                        else{
                            if (key % 2) {
                                ageHeaders = ageHeaders + "<th colspan='2'>" + row_displayAgeName + "</th>";
                            }
                            sexHeaders = sexHeaders + "<th>" + row_displaySexKey + "</th>";
                            dataElementsNo = disaggregated_rows.length;
                        }
                    }

                    row_data_values += "<td>" + row_displayValue + "</td>";
                    if(typeof obj.component[2] !=="undefined"){
                        if(key===0){
                            row_display_third_diaggregate = "("+obj.component[2].value.coding[0].code +")";
                        }

                        if(key+1 ===24 ||key+1 ===48||key+1 ===72 || key+1 ===96){
                            dataElementsNo=24;
                          row_data_values += "</tr><tr>";
                          row_data_values += "<td class='tooltips'>"+indicatorCode+"("+ disaggregated_rows[key+1].component[2].value.coding[0].code +")" +"<span class='tooltiptext'>"+indicatorDisplay+"("+ disaggregated_rows[key+1].component[2].value.coding[0].code +")"+"</span>"+"</td>";
                        }
                    }

                });
            }else{
                if(typeof dataElementsNo !=="undefined"){
                    for(var x=1;x<=dataElementsNo;x++){
                        row_data_values += "<td class='grey'></td>";
                    }
                }
            }
            dataValueToDisplay = "<td class='tooltips'>"+indicatorCode+ row_display_third_diaggregate +"<span class='tooltiptext'>"+indicatorDisplay+ row_display_third_diaggregate+"</span>"+"</td>";
            dataValueToDisplay += row_data_values;
                dataValueToDisplay += "<td>" + total_Value + "</td>";

            dataValueToDisplay += "</tr>";
            reportDataString += dataValueToDisplay;
        });
        ageHeaders = ageHeaders + "<th></th>";
        sexHeaders= sexHeaders+ "<th>Total</th>";

        tableHeader +=ageHeaders +"</tr>";
        sexRow+=sexHeaders + "</tr>";
        tableHeader+=sexRow;

        jq("#display-report").append(tableHeader+endTableHeader + reportDataString + tableFooter);
        jq('#submit-button').show();

    }

    function sendPayLoadInPortionsWithIndicators(dataObject,chunkSize){
        var objectsToSend =[];
        var groupArrayLength = dataObject.group.length;
        var myArray = dataObject.group;
        var setNumber = groupArrayLength/chunkSize;
        for (var i=0,len=myArray.length; i<len; i+=chunkSize){
            var slicedArray = myArray.slice(i,i+chunkSize);
            delete dataObject.group;
            var reportObject =Object.assign({},dataObject);
            reportObject.group =  myArray.slice(i,i+chunkSize);
            objectsToSend.push(reportObject);
        }
        return objectsToSend;
    }

    function stripDisplayAttributes(dataObject){
        var arrayLength = dataObject.group.length;
        if(arrayLength > 0){
            var myArray = dataObject.group;

            for (var i=0; i < myArray.length; i++) {
                var myObject = myArray[i];
                var attr1 = myObject.code.coding[0];
                attr1  = {"code":attr1.code};
                myArray[i].code.coding[0]=attr1;

                if(myObject.stratifier.length > 0){
                    var attr2 = myObject.stratifier[0];
                    var attr2Child =attr2.code;
                    if(attr2Child.length>0){
                        for(var x=0; x < attr2Child.length;x++){
                            var myObject = attr2Child[x];
                            var child = myObject.coding[0];
                            child = {"code":child.code};
                            attr2Child[x].coding[0] = child;
                        }
                    }
                    myArray[i].stratifier[0].code=attr2Child;


                    var attr2Child1 =attr2.stratum;
                    if(attr2Child1.length>0){
                        for(var k=0; k < attr2Child1.length;k++){
                            var myObject = attr2Child1[k];
                            if(typeof myObject.value == "undefined"){
                                var componentObject = myObject.component;
                                if(componentObject.length>0){
                                    for(var j=0; j < componentObject.length;j++){
                                        var child = componentObject[j].value.coding[0];
                                        child = {"code":child.code};

                                        if(typeof componentObject[j].code !== "undefined"){
                                            var child1 = componentObject[j].code.coding[0];
                                            child1 = {"code":child1.code};
                                            attr2Child1[k].component[j].code.coding[0] = child1;
                                        }
                                        attr2Child1[k].component[j].value.coding[0] = child;
                                    }

                                }


                            }else{
                                var child = myObject.value.coding;
                                child = child.map(u =>({"code":u.code}));
                                attr2Child1[k].value.coding = child;
                            }

                        }
                    }
                    myArray[i].stratifier[0].stratum=attr2Child1;
                }

            }
            dataObject.group = [];
            dataObject.group= myArray;
        }
        return dataObject;

    }

    function post(url, dataObject) {
        jq("#loader").show();
        return jq.ajax({
            method: 'POST',
            url: url,
            data: jQuery.param(dataObject),
            headers: {'Content-Type': 'application/json; charset=utf-8'}
        });
    }

    function sendData(jsonData,urlEndPoint) {

        jq.ajax({
            url:'${ui.actionLink("ugandaemrsync","sendReports","sendData")}',
            type: "POST",
            data: {body:jsonData,
                    uuid:urlEndPoint},
            dataType:'json',

            beforeSend : function()
            {
                jq("div#blurred").addClass('show');
                jq("#loader").show();
            },
            success: function (data) {
                var response = data;
                console.log(response);
                if (data.status === "success") {
                    jq().toastmessage('showSuccessToast', response.message);
                    clearDisplayReport();
                } else {
                    jq().toastmessage('showErrorToast', response.message);
                }
                jq("#loader").hide();
                jq("div#blurred").removeClass('show');
            }
        });
    }
    jq(document).ready(function () {
        previewBody =${previewBody};
        uuid ="${reportuuid}";
        merUuids ="${mer_uuids}";
        hmisUuids ="${hmis_uuids}";

        jq("#loader").hide();
        jq("#loading").hide();
        jq("#submit-button").css('display', 'none');
        jq("#surge-report").css('display', 'none');
        var errorMessage = jq('#errorMessage').val();

        if(errorMessage!==""){
            jq().toastmessage('showNoticeToast', errorMessage);
        }

        jq('#sendData').click(function(){
            var strippedPreviewBody = stripDisplayAttributes(previewBody);
            var data = sendPayLoadInPortionsWithIndicators(strippedPreviewBody,5);
            data = JSON.stringify(data);
            sendData(data,uuid);
        });

        jq('#run-button').click(function(){
            jq("#loading").show();
        });

        jq('#run-a-report').click(function(){
            jq("#loading").hide();
        });

       if(previewBody!=null && uuid!=null){
           if(merUuids.split(",").includes(uuid)&& uuid=='e7102e5c-b90d-4a4a-b763-20518eadbae5'){
               displaySurgeReport(previewBody);
           }else if(merUuids.split(",").includes(uuid)) {
               displayMERReport(previewBody);
           }else if(hmisUuids.split(",").includes(uuid)) {
               displayHMISReport(previewBody);
           }
       }
    });
</script>


<div>
    <label style="text-align: center"><h1>Send EMR Reports to DHIS2 </h1></label>

</div>

<%
    def renderingOptions = reportDefinitions
            .collect {
                [ value: it.uuid, label: ui.message(it.name) ]
            }
%>
<div>
    <button id="run-a-report" type="button" style="font-size: 25px" class="confirm" data-toggle="modal"
            data-target="#run-report"  data-whatever="@mdo"> Run a report</button>
</div>
<div class="row">

    <div class="col-md-12">
        <div id="loader">
            <img src="/openmrs/ms/uiframework/resource/uicommons/images/spinner.gif">
            <h1 style="color: aqua">Sending...</h1>
        </div>
        <div id="display-report" style="overflow-y:scroll;">
            <div class='modal-header'> <label style="text-align: center"><h1> ${report_title}</h1></label></div>
            <div id="surge-report">
                <table class="tg">
                    <thead>
                    <tr>
                        <th class="tg-9j11">Indicator&nbsp;&nbsp;&nbsp;Code</th>
                        <th class="tg-9j11" colspan="2">Indicator&nbsp;&nbsp;&nbsp;Name &amp; Disaggregation's</th>
                        <th class="tg-o4ah">Female</th>
                        <th class="tg-o4ah">Male</th>
                        <th class="tg-o4ah">Total</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="tg-0lax" rowspan="3">TX_NEW</td>
                        <td class="tg-zlip" rowspan="3">Number&nbsp;&nbsp;&nbsp;of adults and children newly enrolled on antiretroviral therapy (ART)</td>
                        <td class="tg-8y2u">0-11M</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl" id="TX_NEW-P0M--12M-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">1­ 14 years</td>
                        <td class="tg-ogju" id="TX_NEW-P1Y--15Y-F"> </td>
                        <td class="tg-ogju" id="TX_NEW-P1Y--15Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ years</td>
                        <td class="tg-qnnl" id="TX_NEW-P15Y--9999Y-F"> </td>
                        <td class="tg-qnnl" id="TX_NEW-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-0lax" rowspan="3">VMMC_CIRC</td>
                        <td class="tg-zlip" rowspan="3">Number of Males&nbsp;&nbsp;&nbsp;circumcised</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju" id="VMMC_CIRC-P0Y--P15Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">15-29 Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl" id="VMMC_CIRC-P15Y--P30Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">30+Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju" id="VMMC_CIRC-P30Y--P9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-0lax" rowspan="9">TB_IPT</td>
                        <td class="tg-zlip" rowspan="3">Number of contacts with no signs and symptoms of TB</td>
                        <td class="tg-8y2u"> &lt;5 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">5-14 Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" > </td>
                        <td class="tg-ogju" > </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="3">Number of contacts with no signs and symptoms of TB started&nbsp;&nbsp;&nbsp;on IPT</td>
                        <td class="tg-8y2u"> &lt;5 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TB_IPT-P0Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">5-14 Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="3">Number of ART patients initiated on TB preventive therapy (IPT)</td>
                        <td class="tg-8y2u"> &lt;5 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TB_IPT-P1Y--5Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">5-14 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TB_IPT-P5Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="TB_IPT-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TB_IPT-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-0lax" rowspan="12">TLD</td>
                        <td class="tg-zlip" rowspan="2">Number of newly&nbsp;&nbsp;&nbsp;initiated clients started on TLD</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TLD_STARTED_COARSE-P0Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju"  id="TLD_STARTED_COARSE-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TLD_STARTED_COARSE-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Number&nbsp;&nbsp;&nbsp;of newly initiated clients started on TLD at ART clinic</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TLD_STARTED_ART-P0Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="TLD_STARTED_ART-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TLD_STARTED_ART-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Number of newly initiated clients started on TLD at Mother- Baby&nbsp;&nbsp;&nbsp;Care point</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TLD_STARTED_MOTHER-P0Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju"  id="TLD_STARTED_MOTHER-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TLD_STARTED_MOTHER-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Number of active ART clients transitioned to TLD</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TLD_TRANS_COARSE-P0Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="TLD_TRANS_COARSE-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TLD_TRANS_COARSE-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Number of active ART clients transitioned to TLD at ART clinic</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya"  id="TLD_TRANS_ART-P0Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="TLD_TRANS_ART-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TLD_TRANS_ART-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Number of active ART clients transitioned to TLD at Mother- Baby&nbsp;&nbsp;&nbsp;Care point</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TLD_TRANS_MOTHER-P0Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="TLD_TRANS_MOTHER-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TLD_TRANS_MOTHER-P15Y--9999Y-M" > </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-0lax" rowspan="2">TX_SV(D)</td>
                        <td class="tg-zlip" rowspan="2">Number of clients&nbsp;&nbsp;&nbsp;newly initiated on ART due for second visit in the reporting period</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-qnnl" id="TX_SV_DENOM-P0Y--15Y-F"> </td>
                        <td class="tg-qnnl" id="TX_SV_DENOM-P0Y--15Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="TX_SV_DENOM-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TX_SV_DENOM-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-0lax" rowspan="2">TX_SV(N)</td>
                        <td class="tg-zlip" rowspan="2">Number of clients&nbsp;&nbsp;&nbsp;newly initiated on ART due for second visit </td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-qnnl" id="TX_SV_NUM-P0Y--15Y-F"> </td>
                        <td class="tg-qnnl" id="TX_SV_NUM-P0Y--15Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="TX_SV_NUM-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TX_SV_NUM-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-0lax" rowspan="12">TX_PRO</td>
                        <td class="tg-zlip" rowspan="4">Number of active ART clients transitioned to ABC/3TC/DTG</td>
                        <td class="tg-8y2u"> &lt; 3 Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya" id="TX_PRO_DTG-P0Y--3Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">3-9 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TX_PRO_DTG-P3Y--10Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">10-14Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya" id="TX_PRO_DTG-P10Y--15Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15-19 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TX_PRO_DTG-P15Y--19Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="4">Number of active ART clients&nbsp;&nbsp;&nbsp;transitioned to ABC/3TC/LPV/r</td>
                        <td class="tg-8y2u"> &lt; 3 Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya" id="TX_PRO_LPV-P0Y--3Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">3-9 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TX_PRO_LPV-P3Y--10Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">10-14Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya" id="TX_PRO_LPV-P10Y--15Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15-19 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TX_PRO_LPV-P15Y--19Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="4">Number of active ART clients transitioned to TLD</td>
                        <td class="tg-8y2u"> &lt; 3 Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya" id="TX_PRO_TLD-P0Y--3Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">3-9 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TX_PRO_TLD-P3Y--10Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">10-14Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya" id="TX_PRO_TLD-P10Y--15Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15-19 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TX_PRO_TLD-P15Y--19Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-0lax" rowspan="2">HTS_RECENT</td>
                        <td class="tg-zlip" rowspan="2">Number of newly&nbsp;&nbsp;&nbsp;diagnosed HIV-positive persons who received testing for recent infection with&nbsp;&nbsp;&nbsp;a documented result</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_RECENT-P0Y--15Y-F"> </td>
                        <td class="tg-ogju" id="HTS_RECENT-P0Y--15Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_RECENT-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="HTS_RECENT-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>

                    <tr>
                        <td class="tg-nrix" rowspan="66">HTS_TST (Number of individuals who received HIV Testing Services&nbsp;&nbsp;&nbsp;(HTS) and received their test results infection with a documented result)</td>
                        <td class="tg-zlip" rowspan="2">Social Network&nbsp;&nbsp;&nbsp;Strategy (SNS)-Positive - 1st Positive test</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_SNS-P0Y--P15Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_SNS-P0Y--P15Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_SNS-P15Y--P9999Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_SNS-P15Y--P9999Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Social&nbsp;&nbsp;&nbsp;Network Strategy (SNS)-Positive - Previously tested Positive (Repeat)	</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_SNS-P0Y--P15Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_SNS-P0Y--P15Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_SNS-P15Y--P9999Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_SNS-P15Y--P9999Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Social&nbsp;&nbsp;&nbsp;Network Strategy (SNS)-N</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_SNS-P0Y--P15Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_SNS-P0Y--P15Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_SNS-P15Y--P9999Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_SNS-P15Y--P9999Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Index&nbsp;&nbsp;&nbsp;Facility Client testing - Positive - 1st Positive test</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_FAC-P0Y--P15Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_FAC-P0Y--P15Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_FAC-P15Y--P9999Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_FAC-P15Y--P9999Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Index&nbsp;&nbsp;&nbsp;Facility Client testing - Positive - Previously tested Positive (Repeat)	</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_FAC-P0Y--P15Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_FAC-P0Y--P15Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_FAC-P15Y--P9999Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_FAC-P15Y--P9999Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Index&nbsp;&nbsp;&nbsp;Facility Client testing - N</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_FAC-P0Y--P15Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_FAC-P0Y--P15Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_FAC-P15Y--P9999Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_FAC-P15Y--P9999Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">PMTCT&nbsp;&nbsp;&nbsp;(ANC 1 only) - Positive - 1st Positive test</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_ANC-P0Y--P15Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_ANC-P0Y--P15Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_ANC-P15Y--P9999Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_ANC-P15Y--P9999Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">PMTCT&nbsp;&nbsp;&nbsp;(ANC 1 only) - Positive - Previously tested Positive (Repeat)	</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_ANC-P0Y--P15Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_ANC-P0Y--P15Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_ANC-P15Y--P9999Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_ANC-P15Y--P9999Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">PMTCT&nbsp;&nbsp;&nbsp;(ANC 1 only) - N</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_ANC-P0Y--P15Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_ANC-P0Y--P15Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_ANC-P15Y--P9999Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_ANC-P15Y--P9999Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">PMTCT&nbsp;&nbsp;&nbsp;– Post ANC  Pregnancy/L&amp;D/BF -&nbsp;&nbsp;&nbsp;Positive - 1st Positive test</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_POST-P0Y--P15Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_POST-P0Y--P15Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_POST-P15Y--P9999Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_POST-P15Y--P9999Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">PMTCT&nbsp;&nbsp;&nbsp;– Post ANC  Pregnancy/L&amp;D/BF -&nbsp;&nbsp;&nbsp;Positive - Previously tested Positive (Repeat)	</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_POST-P0Y--P15Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_POST-P0Y--P15Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_POST-P15Y--P9999Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_POST-P15Y--P9999Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">PMTCT&nbsp;&nbsp;&nbsp;– Post ANC  Pregnancy/L&amp;D/BF -&nbsp;&nbsp;&nbsp;N</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_POST-P0Y--P15Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_POST-P0Y--P15Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_POST-P15Y--P9999Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_PMTCT_POST-P15Y--P9999Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Malnutrition&nbsp;&nbsp;&nbsp;- 1st Positive test</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_MAL-P0Y--P15Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_MAL-P0Y--P15Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_MAL-P15Y--P9999Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_MAL-P15Y--P9999Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Malnutrition-&nbsp;&nbsp;&nbsp;- Previously tested Positive (Repeat)	</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_MAL-P0Y--P15Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_MAL-P0Y--P15Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_MAL-P15Y--P9999Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_MAL-P15Y--P9999Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Malnutrition&nbsp;&nbsp;&nbsp;- N</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_MAL-P0Y--P15Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_MAL-P0Y--P15Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_MAL-P15Y--P9999Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_MAL-P15Y--P9999Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Pediatric&nbsp;&nbsp;&nbsp;- 1st Positive test</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_PED-P0Y--P15Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_PED-P0Y--P15Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_PED-P15Y--P9999Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_PED-P15Y--P9999Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Pediatric-&nbsp;&nbsp;&nbsp;Previously tested Positive (Repeat)	</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_PED-P0Y--P15Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_PED-P0Y--P15Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_PED-P15Y--P9999Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_PED-P15Y--P9999Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Pediatric&nbsp;&nbsp;&nbsp;- N</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_PED-P0Y--P15Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_PED-P0Y--P15Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_PED-P15Y--P9999Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_PED-P15Y--P9999Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">TB&nbsp;&nbsp;&nbsp;Clinic - 1st Positive test</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_TB-P0Y--P15Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_TB-P0Y--P15Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_TB-P15Y--P9999Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_TB-P15Y--P9999Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">TB&nbsp;&nbsp;&nbsp;Clinic Previously tested Positive (Repeat)	</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_TB-P0Y--P15Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_TB-P0Y--P15Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_TB-P15Y--P9999Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_TB-P15Y--P9999Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">TB&nbsp;&nbsp;&nbsp;Clinic - N</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_TB-P0Y--P15Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_TB-P0Y--P15Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_TB-P15Y--P9999Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_TB-P15Y--P9999Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Other&nbsp;&nbsp;&nbsp;PITC- 1st Positive test</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_OTHER_PITC-P0Y--P15Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_OTHER_PITC-P0Y--P15Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_OTHER_PITC-P15Y--P9999Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_OTHER_PITC-P15Y--P9999Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Other&nbsp;&nbsp;&nbsp;PITC- Previously tested Positive (Repeat)	</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_OTHER_PITC-P0Y--P15Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_OTHER_PITC-P0Y--P15Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_OTHER_PITC-P15Y--P9999Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_OTHER_PITC-P15Y--P9999Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Other&nbsp;&nbsp;&nbsp;PITC- N</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_OTHER_PITC-P0Y--P15Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_OTHER_PITC-P0Y--P15Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_OTHER_PITC-P15Y--P9999Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_OTHER_PITC-P15Y--P9999Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Index&nbsp;&nbsp;&nbsp;Community Client testing- 1st Positive test</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_COM-P0Y--P15Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_COM-P0Y--P15Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_COM-P15Y--P9999Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_COM-P15Y--P9999Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Index&nbsp;&nbsp;&nbsp;Community Client testing - Previously tested Positive (Repeat)	</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_COM-P0Y--P15Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_COM-P0Y--P15Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_COM-P15Y--P9999Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_COM-P15Y--P9999Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Index&nbsp;&nbsp;&nbsp;Community Client testing- N</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_COM-P0Y--P15Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_COM-P0Y--P15Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_COM-P15Y--P9999Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_INDEX_COM-P15Y--P9999Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">VCT&nbsp;&nbsp;&nbsp;COMMUNITY- 1st Positive test</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_VCT-P0Y--P15Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_VCT-P0Y--P15Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_VCT-P15Y--P9999Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_VCT-P15Y--P9999Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">VCT&nbsp;&nbsp;&nbsp;COMMUNITY - Previously tested Positive (Repeat)	</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_VCT-P0Y--P15Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_VCT-P0Y--P15Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_VCT-P15Y--P9999Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_VCT-P15Y--P9999Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">VCT&nbsp;&nbsp;&nbsp;COMMUNITY- N</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_VCT-P0Y--P15Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_VCT-P0Y--P15Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_VCT-P15Y--P9999Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_VCT-P15Y--P9999Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">MOBILE&nbsp;&nbsp;&nbsp;COMMUNITY- 1st Positive test</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_MOB-P0Y--P15Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_MOB-P0Y--P15Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_MOB-P15Y--P9999Y-F-P1"> </td>
                        <td class="tg-ogju" id="HTS_TST_MOB-P15Y--P9999Y-M-P1"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">MOBILE&nbsp;&nbsp;&nbsp;COMMUNITY- Previously tested Positive (Repeat)	</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_MOB-P0Y--P15Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_MOB-P0Y--P15Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_MOB-P15Y--P9999Y-F-P2"> </td>
                        <td class="tg-ogju" id="HTS_TST_MOB-P15Y--P9999Y-M-P2"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">MOBILE&nbsp;&nbsp;&nbsp;COMMUNITY- N</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_TST_MOB-P0Y--P15Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_MOB-P0Y--P15Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_TST_MOB-P15Y--P9999Y-F-N"> </td>
                        <td class="tg-ogju" id="HTS_TST_MOB-P15Y--P9999Y-M-N"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>

                    </tbody>
                </table>
            </div>
        </div>
        <div id="submit-button">
            <p></p><span id="sendData"  class="button confirm right"> Submit </span>
        </div>
        <div id="blurred">
        </div>
    </div>

</div>

<div class="modal fade" id="run-report" tabindex="-1" role="dialog"
     aria-labelledby="addEditSyncTaskTypeModelLabel"
     aria-hidden="true">
    <div class="modal-dialog  modal-md" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="addEditSyncTaskTypeModelLabel">Run the Report</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div id="loading">
                <img src="/openmrs/ms/uiframework/resource/uicommons/images/spinner.gif">
                <span>Running...</span>
            </div>
            <div>
                <form method="post" id="sendReports">
                    <fieldset>
                        ${ui.includeFragment("uicommons","field/dropDown",[
                                formFieldName: "reportDefinition",
                                label: "Report",
                                hideEmptyLabel: false,
                                options: renderingOptions

                        ])}

                        ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                                formFieldName: "startDate",
                                label: "StartDate",
                                useTime: false,
                                defaultDate: ""
                        ])}
                        ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                                formFieldName: "endDate",
                                label: "EndDate",
                                useTime: false,
                                defaultDate: ""
                        ])}

                        <p></p>
                        <span>
                            <button id="run-button" type="submit" class="confirm right" ng-class="{disabled: submitting}" ng-disabled="submitting">
                                <i class="icon-play"></i>
                                Run
                            </button>
                        </span>

                    </fieldset>
                    <input type="hidden" name="errorMessage" id="errorMessage" value="${errorMessage}">
                </form>
            </div>
        </div>
    </div>
</div>
