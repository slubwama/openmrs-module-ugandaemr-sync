<%
    def breadcrumbMiddle = breadcrumbOverride ?: '';
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.app.systemAdministration.label")}", link: '/' + OPENMRS_CONTEXT_PATH + '/coreapps/systemadministration/systemAdministration.page'},
        { label: "UgandaEMR Sync", link: '/' + OPENMRS_CONTEXT_PATH + '/ugandaemrsync/ugandaemrsync.page'},
        { label: "Send Reports"}
    ];

    var previewBody;

    function clearDisplayReport(){
        jq("#display-report").empty();
        jq('#submit-button').empty();
    }
    function displayReport(report){
        var reportDataString="";
        var tableHeader = "<table><thead><tr><th>Indicator</th><th>Data Element</th><th>Value</th></thead><tbody>";
        var tableFooter = "</tbody></table>";

        jq.each(report.group, function (index, rowValue) {
            var total_Display_Name="";
            var dataValueToDisplay = "";
            dataValueToDisplay += "<tr>";

                total_Display_Name = rowValue.stratifier[0].code[0].coding[0].display;
                var total_Display_Value = rowValue.measureScore.value;
                var disaggregated_rows = rowValue.stratifier[0].stratum;

            var rowspanAttribute="rowspan= \""+disaggregated_rows.length+"\"";

            jq.each(disaggregated_rows,function(key,obj){
                var row_displayName = obj.value.coding[0].display;
                var row_displayValue = obj.measureScore.value;
                dataValueToDisplay += "<tr>";
                if(key==0){
                    dataValueToDisplay += "<th " + rowspanAttribute+ " width='20%'>" +total_Display_Name +"</th>";
                }
                dataValueToDisplay += "<td>" +row_displayName +"</td>";
                dataValueToDisplay += "<td>" +row_displayValue + "</td>";
                dataValueToDisplay += "</tr>";
            });

            reportDataString += dataValueToDisplay;
        });

        jq("#display-report").append(tableHeader + reportDataString + tableFooter);
        jq('#submit-button').show();


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

    function sendData(jsonData) {

        jq.ajax({
            url:'${ui.actionLink("ugandaemrsync","sendReports","sendData")}',
            type: "POST",
            data: {body:jsonData},
            dataType:'json',

            beforeSend : function()
            {
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
            }
        });
    }
    jq(document).ready(function () {
        previewBody =${previewBody};

        jq("#loader").hide();
        jq("#submit-button").css('display', 'none');
        var errorMessage = jq('#errorMessage').val();

        if(errorMessage!==""){
            jq().toastmessage('showNoticeToast', errorMessage);
        }

        jq('#sendData').click(function(){
            var data = JSON.stringify(previewBody);
            sendData(data);
        });

       if(previewBody!=null){
           displayReport(previewBody);
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
<div class="row">
    <div class="col-md-4">
        <form method="post" id="sendReports">
            <fieldset>
                <legend> Run the Report</legend>
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
                        <button type="submit" class="confirm right" ng-class="{disabled: submitting}" ng-disabled="submitting">
                            <i class="icon-play"></i>
                            Run
                        </button>
                    </span>

            </fieldset>
            <input type="hidden" name="errorMessage" id="errorMessage" value="${errorMessage}">
        </form>
    </div>
    <div class="col-md-8">
        <div id="loader">
            <img src="/openmrs/ms/uiframework/resource/uicommons/images/spinner.gif">
        </div>
        <div id="display-report" style="height:500px;overflow-y:scroll;">
            <div class='modal-header'> <label style="text-align: center"><h1> ${report_title}</h1></label></div>
        </div>
        <div id="submit-button">
            <p></p><span id="sendData"  class="button confirm right"> Submit </span>
        </div>
    </div>

</div>


