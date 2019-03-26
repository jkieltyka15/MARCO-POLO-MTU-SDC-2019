const functions = require('firebase-functions');

// Listen for changes in all documents in the 'users' collection
exports.useWildcard = functions.firestore
    .document('Gunshots/{docId}')
    .onCreate((change, context) => {
      // If we set `/users/marie` to {name: "Marie"} then
      // context.params.userId == "marie"
      // ... and ...
      // change.after.data() == {name: "Marie"}
    });
