
var synctasktype = synctasktype || {};

synctasktype.completeQueueDialog = null;
synctasktype.addSyncTaskTypeDialog = null;
synctasktype.readMessageDialog = null;
synctasktype.alert_message_id = null;
synctasktype.message = null;
synctasktype.createMessageDialog = null;

synctasktype.showAddSyncTaskTypeDialog = function (patientId) {
    synctasktype.patientId = patientId;
    if (synctasktype.addSyncTaskTypeDialog == null) {
        synctasktype.createAddSyncTaskTypeDialog();
    }
    synctasktype.addSyncTaskTypeDialog.show();
};

synctasktype.closeAddsyncTaskTypeDialog = function () {
    synctasktype.addSyncTaskTypeDialog.close();
};


synctasktype.createAddSyncTaskTypeDialog = function () {
    synctasktype.addSyncTaskTypeDialog = emr.setupConfirmationDialog({
        selector: '#add_sync_task_type_dialog',
        actions: {
            cancel: function () {
                synctasktype.addSyncTaskTypeDialog.close();
            }
        }
    });

    synctasktype.addSyncTaskTypeDialog.close();
}