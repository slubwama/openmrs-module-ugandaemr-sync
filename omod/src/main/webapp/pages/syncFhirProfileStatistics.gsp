<%
    // although called "patientDashboard" this is actually the patient visits screen, and clinicianfacing/patient is the main patient dashboard
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeJavascript("uicommons", "bootstrap-collapse.js")
    ui.includeJavascript("uicommons", "bootstrap-transition.js")
    ui.includeCss("uicommons", "styleguide/index.styles")
    ui.includeCss("uicommons", "datatables/dataTables_jui.styles")
%>
<script type="text/javascript">
    var breadcrumbs = [
        {icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm'},
        {
            label: "${ ui.message("coreapps.app.systemAdministration.label")}",
            link: '/' + OPENMRS_CONTEXT_PATH + '/coreapps/systemadministration/systemAdministration.page'
        },
        {label: "UgandaEMR Sync", link: '/' + OPENMRS_CONTEXT_PATH + '/ugandaemrsync/ugandaemrsync.page'},
        {label: "Sync Profile Statistics"}
    ];

    jq(document).ready(function () {
    });
</script>

<script>

    if (jQuery) {

        jq(document).ready(function () {

            populateSelectProfileList(queryRestData("/ws/rest/v1/syncfhirprofile?v=full"))

            jq("#syncProfile").change(function () {
                var profile = jq("#syncProfile :selected").val();
                displayCaseData(queryRestData("/ws/rest/v1/syncfhircase?profile=" + profile + "&v=full"));

                displayProfileLogData(queryRestData("/ws/rest/v1/syncfhirprofilelog?profile=" + profile + "&v=full"));

                displayResourcesData(queryRestData("/ws/rest/v1/syncfhirresource?profile=" + profile + "&v=full"))
            });
        });
    }


    function queryRestData(url) {
        var responseDate = null;
        jq.ajax({
            type: "GET",
            url: '/' + OPENMRS_CONTEXT_PATH + url,
            dataType: "json",
            async: false,
            success: function (data) {
                responseDate = data;
            }
        });
        return responseDate;
    }

    function populateSelectProfileList(response) {
        jq("#syncProfile").html("");
        var selectOptions = "<option value=\"\">Select Primary Resource Type</option>";

        jq.each(response.results, function (index, element) {

                selectOptions += "<option value='" + element.uuid + "'>" + element.name + "</option>";
                ;
            }
        );

        jq("#syncProfile").html("");
        jq("#syncProfile").append(selectOptions);

    }


    function displayCaseData(response) {
        jq("#sync_fhir_case_table").html("");
        var dataRows = "";
        var headercases = "<table class='table table-striped table-bordered table-sm'><thead><tr><th>PATIENT</th><th>PROFILE IDENTIFIER</th><th>LAST UPDATED</th><th>DATE CREATED</th></tr></thead><tbody>";

        var footer = "</tbody></table>";

        var dataToDisplay = [];

        if (response.results.length > 0) {
            dataToDisplay = response.results.sort(function (a, b) {
                return a.dateCreated - b.dateCreated;
            });
        }

        jq.each(dataToDisplay, function (index, element) {
                var profileCaseElement = element;
                var dataRowTable = "";


                dataRowTable += "<tr>";
                dataRowTable += "<td>" + profileCaseElement.patient.display + "</td>";
                dataRowTable += "<td>" + profileCaseElement.caseIdentifier + "</td>";
                dataRowTable += "<td>" + profileCaseElement.lastUpdateDate + "</td>";
                dataRowTable += "<td>" + profileCaseElement.dateCreated + "</td>";
                dataRowTable += "</tr>";

                dataRows += dataRowTable;
            }
        );

        jq("#sync_fhir_case_table").html("");
        jq("#sync_fhir_case_table").append(headercases + dataRows + footer);

    }

    function displayProfileLogData(response) {
        jq("#sync_fhir_profile_log_table").html("");
        var profileLogDataRows = "";

        var header = "<table><thead><tr><th>RESOURCE</th><th>LAST SYNC DATE</th><th>NO. OF RESOURCE</th></tr></thead><tbody>";
        var footer = "</tbody></table>";

        var dataToDisplay = [];

        if (response.results.length > 0) {
            dataToDisplay = response.results.sort(function (a, b) {
                return a.dateCreated - b.dateCreated;
            });
        }

        jq.each(dataToDisplay, function (index, element) {
                var profileLogElement = element;
                var dataRowTable = "";
                dataRowTable += "<tr>";
                dataRowTable += "<td>" + profileLogElement.resourceType + "</td>";
                dataRowTable += "<td>" + profileLogElement.lastGenerationDate + "</td>";
                dataRowTable += "<td>" + profileLogElement.numberOfResources + "</td>";
                dataRowTable += "</tr>";

                profileLogDataRows += dataRowTable;
            }
        );

        jq("#sync_fhir_profile_log_table").html("");
        jq("#sync_fhir_profile_log_table").append(header + profileLogDataRows + footer);
    }

    function displayResourcesData(response) {
        jq("#sync_fhir_resource_table").html("");
        var profileLogDataRows = "";

        var header = "<table><thead><tr><th>RESOURCE ID</th><th>DATE CREATED</th><th>SYNCED TO SERVER</th><th>SYNCED DATED</th><th>PURGE DATE</th></tr></thead><tbody>";
        var footer = "</tbody></table>";

        var dataToDisplay = [];

        if (response.results.length > 0) {
            dataToDisplay = response.results.sort(function (a, b) {
                return a.dateCreated - b.dateCreated;
            });
        }

        jq.each(dataToDisplay, function (index, element) {
                var profileLogElement = element;
                var dataRowTable = "";
                dataRowTable += "<tr>";
                dataRowTable += "<td>" + profileLogElement.uuid + "</td>";
                dataRowTable += "<td>" + profileLogElement.dateCreated + "</td>";
                dataRowTable += "<td>" + profileLogElement.synced + "</td>";
                dataRowTable += "<td>" + profileLogElement.dateSynced + "</td>";
                dataRowTable += "<td>" + profileLogElement.expiryDate + "</td>";
                dataRowTable += "</tr>";

                profileLogDataRows += dataRowTable;
            }
        );

        jq("#sync_fhir_resource_table").html("");
        jq("#sync_fhir_resource_table").append(header + profileLogDataRows + footer);
    }

</script>
<style>

</style>

<div class="card">
    <div class="card-header">
        <div class="row">
            <div class="col-3">
                <label>Sync FHIR Profile</label>
                <select class="form-control" name="syncProfile" id="syncProfile"></select>
            </div>
        </div>

    </div>

    <div class="card-body">

        <div class="card-body">
            <ul class="nav nav-tabs nav-fill" id="myTab" role="tablist">
                <li class="nav-item">
                    <a class="nav-item nav-link active" id="home-tab" data-toggle="tab" href="#fhir-case" role="tab"
                       aria-controls="fhir-case-tab" aria-selected="true">Patient in the Exchange Profile
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link" id="resource-tab" data-toggle="tab" href="#resource-list" role="tab"
                       aria-controls="resource-list-tab" aria-selected="false">Resources
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link" id="profile-log-tab" data-toggle="tab" href="#profile-log-list" role="tab"
                       aria-controls="profile-log-list-tab" aria-selected="false">Profile Logs
                    </a>
                </li>
            </ul>

            <div class="tab-content" id="myTabContent">
                <div class="tab-pane fade show active" id="fhir-case" role="tabpanel"
                     aria-labelledby="fhir-case-tab">
                    <div class="info-body">
                        <div id="sync_fhir_case_table">
                        </div>
                    </div>
                </div>

                <div class="tab-pane fade" id="resource-list" role="tabpanel"
                     aria-labelledby="resource-list-tab">
                    <div class="info-body">
                        <div id="sync_fhir_resource_table">
                        </div>
                    </div>
                </div>

                <div class="tab-pane fade" id="profile-log-list" role="tabpanel"
                     aria-labelledby="profile-log-list-tab">
                    <div class="info-body">
                        <div id="sync_fhir_profile_log_table">
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>


