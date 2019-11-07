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
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.app.systemAdministration.label")}", link: '/' + OPENMRS_CONTEXT_PATH + '/coreapps/systemadministration/systemAdministration.page'},
        { label: "UgandaEMR Sync", link: '/' + OPENMRS_CONTEXT_PATH + '/ugandaemrsync/ugandaemrsync.page'},
        { label: "Sync Task Logs"}
    ];

    jq(document).ready(function () {

        jq("#okay").click(function () {
            patientqueue.createReadMessageDialog();
        });
    });
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

#patient-search {
    min-width: 55%;
    color: #363463;
    display: block;
    padding: 5px 10px;
    margin: 0;
    margin-top: 5px;
    background-color: #FFF;
    border: 1px solid #dddddd;
}

.div-table {
    display: table;
    width: 100%;
}

.div-row {
    display: table-row;
    width: 100%;
}

.div-col1 {
    display: table-cell;
    margin-left: auto;
    margin-right: auto;
    width: 100%;
}

.div-col2 {
    display: table-cell;
    margin-right: auto;
    margin-left: auto;
    width: 50%;
}

.div-col3 {
    display: table-cell;
    margin-right: auto;
    margin-left: auto;
    width: 33%;
}

.div-col4 {
    display: table-cell;
    margin-right: auto;
    margin-left: auto;
    width: 25%;
}
</style>

<div class="dashboard clear">
    <div class="info-section">
        <div class="info-header">
            <label><h2>Sync Task Logs</h2></label>
        </div>

        <div class="info-body">
            <table>
                <thead>
                <tr>
                    <th>Sync Task Id</th>
                    <th>Sync Task Type</th>
                    <th>Sync Task Data Type</th>
                    <th>Sync Task</th>
                    <th>Status</th>
                    <th>Status Code</th>
                    <th>Require Action</th>
                    <th>Action Completed</th>
                    <th>Date Sent</th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (syncTask.size() > 0) {
                    syncTask.each { %>
                <tr>
                    <td>${it.syncTaskId}</td>
                    <td>${it.syncTaskType.name}</td>
                    <td>${it.syncTaskType.dataType}</td>
                    <td>${it.syncTask}</td>
                    <td>${it.status}</td>
                    <td>${it.statusCode}</td>
                    <td>${it.requireAction}</td>
                    <td>${it.actionCompleted}</td>
                    <td>${it.dateSent}</td>
                    <% }
                    } %>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>
