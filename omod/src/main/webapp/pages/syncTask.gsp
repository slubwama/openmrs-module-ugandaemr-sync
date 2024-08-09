<%
    // although called "patientDashboard" this is actually the patient visits screen, and clinicianfacing/patient is the main patient dashboard
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeJavascript("uicommons", "bootstrap-collapse.js")
    ui.includeJavascript("uicommons", "bootstrap-transition.js")
    ui.includeCss("uicommons", "styleguide/index.styles")
    ui.includeCss("uicommons", "datatables/dataTables_jui.styles")
    ui.includeJavascript("ugandaemrsync", "synctasktype.js")
%>
<script type="text/javascript">

</script>
<script type="text/javascript">
    var breadcrumbs = [
        {icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm'},
        {
            label: "${ ui.message("coreapps.app.systemAdministration.label")}",
            link: '/' + OPENMRS_CONTEXT_PATH + '/coreapps/systemadministration/systemAdministration.page'
        },
        {label: "UgandaEMR Sync", link: '/' + OPENMRS_CONTEXT_PATH + '/ugandaemrsync/ugandaemrsync.page'},
        {label: "Sync Task Logs"}
    ];

    if (jQuery) {

        jq(document).ready(function () {

            jq("#sync_log_table").append("No Data to Display");

            jq("#searchTaskLog").click(function () {
                var syncTaskTpe = jq("#syncTaskType :selected").val();
                var fromDate = jq("#fromDate").val();
                var toDate = jq("#toDate").val();
                var statusCode = jq("#statusCode").val();
                var limit = jq("#queryLimit").val();

                var searchQuery = "";

                if (syncTaskTpe !== null && syncTaskTpe !== "") {
                    searchQuery += "&type=" + syncTaskTpe;
                }

                if (fromDate != null && fromDate !== "") {
                    searchQuery += "&fromDate=" + fromDate;
                }

                if (toDate != null && toDate !== "") {
                    searchQuery += "&toDate=" + toDate;
                }

                if (statusCode != null && statusCode !== "") {
                    searchQuery += "&status=" + statusCode;
                }

                if (limit != null && limit !== "") {
                    searchQuery += "&limit=" + limit;
                }

                displaySyncTaskLogData(queryRestData("/ws/rest/v1/synctask?v=full" + searchQuery));

            });

            jq("#getnextLimit").click(function () {
                var syncTaskTpe = jq("#syncTaskType :selected").val();
                var fromDate = jq("#fromDate").val();
                var toDate = jq("#toDate").val();
                var statusCode = jq("#statusCode").val();
                var limit = jq("#queryLimit").val();

                var searchQuery = "";

                if (syncTaskTpe !== null && syncTaskTpe !== "") {
                    searchQuery += "&type=" + syncTaskTpe;
                }

                if (fromDate != null && fromDate !== "") {
                    searchQuery += "&fromDate=" + fromDate;
                }

                if (toDate != null && toDate !== "") {
                    searchQuery += "&toDate=" + toDate;
                }

                if (statusCode != null && statusCode !== "") {
                    searchQuery += "&status=" + statusCode;
                }

                if (limit != null && limit !== "") {
                    searchQuery += "&limit=" + limit;
                }

                displaySyncTaskLogData(queryRestData("/ws/rest/v1/synctask?v=full" + searchQuery));

            });

            setQueryLimit(queryRestData("/ws/rest/v1/systemsetting?q=webservices.rest.maxResultsAbsolute&v=custom:(value,uuid)"));
            populateSelectSyncTaskTypeList(queryRestData("/ws/rest/v1/synctasktype?v=full"));
        });


        function getNextLimit(url) {
            displaySyncTaskLogData(queryRestData(url));
        }

        function queryRestData(url) {
            var responseDate = null;
            jq.ajax({
                type: "GET",
                url: '/' + OPENMRS_CONTEXT_PATH + url,
                dataType: "json",
                async: false,
                success: function (response) {
                    responseDate = response;
                },
                error: function (response) {
                    jq().toastmessage('showErrorToast', response.responseJSON.error.message);
                }
            });
            return responseDate;
        }

        function setQueryLimit(response) {
            jq('#queryLimit').attr('max', response.results[0].value);
            jq('#queryLimit').attr('placeholder', "0 to " + response.results[0].value);
        }

        function populateSelectSyncTaskTypeList(response) {
            jq("#syncTaskType").html("");
            var selectOptions = "<option value=\"\">Select Sync TaskType </option>";

            jq.each(response.results, function (index, element) {
                    selectOptions += "<option value='" + element.uuid + "'>" + element.name + "</option>";
                }
            );

            jq("#syncTaskType").html("");
            jq("#syncTaskType").append(selectOptions);

        }

        function processPagination(response) {
            var next = "";
            var previous = "";
            if (response != null && response.hasOwnProperty("links")) {
                response.links.forEach(function (link) {
                    var url = link.uri.split("/openmrs")[1]
                    if (link.rel === "next") {
                        next += "<button title=\"Edit Result\" onclick='getNextLimit(\"" + url + "\")'>" + link.rel.toUpperCase() + "</button>&nbsp;&nbsp;&nbsp;&nbsp;";
                    } else {
                        previous += "<button title=\"Edit Result\" onclick='getNextLimit(\"" + url + "\")'>" + link.rel.toUpperCase() + "</button>&nbsp;&nbsp;&nbsp;&nbsp;";
                    }
                });
                return previous + next
            } else {
                return ""
            }
        }


        function displaySyncTaskLogData(response) {
            jq("#sync_log_table").html("");
            var profileLogDataRows = "";

            var header = "<table><thead><tr><th>Sync Task</th><th>Sync Task Type</th><th>Status</th><th>Status Code</th><th>Require Action</th><th>Action Completed</th><th>Date Sent</th></tr></thead><tbody>";
            var footer = "</tbody></table>";
            var pagination = "";

            var dataToDisplay = [];

            if (response != null && response.results.length > 0) {
                dataToDisplay = response.results.sort(function (a, b) {
                    return a.dateCreated - b.dateCreated;
                });
                pagination = processPagination(response);
            }

            jq.each(dataToDisplay, function (index, element) {
                    var profileLogElement = element;
                    var dataRowTable = "";
                    dataRowTable += "<tr>";
                    dataRowTable += "<td>" + profileLogElement.syncTask + "</td>";
                    dataRowTable += "<td>" + profileLogElement.syncTaskType.name + "</td>";
                    dataRowTable += "<td>" + profileLogElement.status + "</td>";
                    dataRowTable += "<td>" + profileLogElement.statusCode + "</td>";
                    dataRowTable += "<td>" + profileLogElement.requireAction + "</td>";
                    dataRowTable += "<td>" + profileLogElement.actionCompleted + "</td>";
                    dataRowTable += "<td>" + profileLogElement.dateSent + "</td>";
                    dataRowTable += "</tr>";

                    profileLogDataRows += dataRowTable;
                }
            );

            if (dataToDisplay.length > 0) {
                jq("#sync_log_table").append(pagination + header + profileLogDataRows + footer + pagination);
            } else {
                jq("#sync_log_table").append("No Data to Display");
            }
        }
    }

</script>
<style>
.dashboard .que-container {
    display: inline;
    float: left;
    width: 65%;
    margin: 0 1.04167%;
}

.dashboard .alert-container {
    display: inline;
    float: left;
    width: 30%;
    margin: 0 1.04167%;
}

.dashboard .action-section ul {
    background: #63343c;
    color: white;
    padding: 7px;
}
</style>

<div class="dashboard clear">
    <div class="info-section">
        <div class="info-header">
            <label><h2>Sync Task Logs</h2></label>
        </div>

        <div class="card">
            <div class="card-header">
                <div class="row">
                    <div class="col-2">
                        <label>Sync Task Type</label>
                        <select class="form-control" name="syncTaskType" id="syncTaskType"></select>
                    </div>

                    <div class="col-2">
                        <label>From Date</label>
                        <input class="form-control" type="date" name="code" id="fromDate"/>
                    </div>

                    <div class="col-2">
                        <label>To Date</label>
                        <input class="form-control" type="date" name="code" id="toDate"/>
                    </div>

                    <div class="col-2">
                        <label>Status Code</label>
                        <input class="form-control" type="number" name="statusCode" id="statusCode"
                               placeholder="eg 200"/>
                    </div>

                    <div class="col-2">
                        <label>Query Limit</label>
                        <input class="form-control" type="number" max="100" name="queryLimit" id="queryLimit"
                               placeholder=""/>
                    </div>

                    <div class="col-2">
                        <br/>
                        <button name="searchTaskLog" id="searchTaskLog">Search</button>
                    </div>
                </div>

            </div>

            <div class="card-body">
                <div id="sync_log_table">

                </div>
            </div>
        </div>
    </div>
</div>

