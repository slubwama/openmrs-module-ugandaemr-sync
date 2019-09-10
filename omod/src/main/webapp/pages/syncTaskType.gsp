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
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.app.systemAdministration.label")}", link: '/' + OPENMRS_CONTEXT_PATH + '/coreapps/systemadministration/systemAdministration.page'},
        { label: "UgandaEMR Sync", link: '/' + OPENMRS_CONTEXT_PATH + '/ugandaemrsync/ugandaemrsync.page'},
        { label: "Sync Task Type"}
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

        <div class="info-header"><span style="right:auto;width: 70%;font-weight: bold"><h3>Add Sync Task Type</h3>
        </span> <span style="right:auto;width: 20%;font-weight: bold"><a
                onclick="synctasktype.createAddSyncTaskTypeDialog()"><i class="icon-plus-sign-alt"></i></a></span>
        </div>

        <div>
            <form method="post">
                <fieldset style="min-width: 40%">

                    <div class="div-table">
                        <div class="div-row">
                            <div class="div-col4">
                                <label>Sync Task Type Name</label>
                                <input type=" text" name="name" id="name">
                            </div>

                            <div class="div-col4">
                                <label>Data Type</label>
                                <select name="datatype">
                                    <option value=""></option>
                                    <option value="java.lang.Boolean">java.lang.Boolean</option>
                                    <option value="java.lang.Character">java.lang.Character</option>
                                    <option value="java.lang.Float">java.lang.Float</option>
                                    <option value="java.lang.Integer">java.lang.Integer</option>
                                    <option value="java.lang.String">java.lang.String</option>
                                    <option value="org.openmrs.Concept">org.openmrs.Concept</option>
                                    <option value="org.openmrs.Drug">org.openmrs.Drug</option>
                                    <option value="org.openmrs.Encounter">org.openmrs.Encounter</option>
                                    <option value="org.openmrs.Order">org.openmrs.Order</option>
                                    <option value="org.openmrs.Location">org.openmrs.Location</option>
                                    <option value="org.openmrs.Patient">org.openmrs.Patient</option>
                                    <option value="org.openmrs.Person">org.openmrs.Person</option>
                                    <option value="org.openmrs.ProgramWorkflow">org.openmrs.ProgramWorkflow</option>
                                    <option value="org.openmrs.Provider">org.openmrs.Provider</option>
                                    <option value="org.openmrs.User">org.openmrs.User</option>
                                    <option value="org.openmrs.util.AttributableDate">org.openmrs.util.AttributableDate</option>
                                </select>
                            </div>

                            <div class="div-col4">
                                <label>Data Type Identifier (eg uuid for enounter type)</label>
                                <input type=" text" name="datatypeid" id="datatypeid">
                            </div>
                            <div class="dialog-content form div-col4">
                                <br/>
                                <input type="submit" class="confirm" value="${ui.message("Save")}">
                            </div>
                        </div>
                    </div>

                </fieldset>
            </form>
        </div>

        <div class="info-body">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>NAME</th>
                    <th>URL</th>
                    <th>DATE</th>
                    <th>UUID</th>
                    <th>ACTION</th>
                </tr>
                </thead>
                <tbody>
                <% if (syncTaskTypes.size() > 0) {
                    syncTaskTypes.each { %>
                <tr>
                    <td>${it.syncTaskTypeId}</td>
                    <td>${it.name}</td>
                    <td>${it.url}</td>
                    <td>${it.dateCreated}</td>
                    <td>${it.uuid}</td>
                    <td><i class="icon-trash" title="Delete" onclick="location.href = ''"></i></td>
                    <% }
                    } %>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>
