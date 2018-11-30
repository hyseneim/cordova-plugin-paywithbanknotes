var exec = require('cordova/exec');

var payWithBankNotes = {

    createAccount: function(payload, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, 'PayWithBankNotesPlugin', 'sendPayload', [payload]);
    },

    updateContact: function(payload, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, 'PayWithBankNotesPlugin', 'updateContact', [payload]);
    }

};

module.exports = payWithBankNotes;
