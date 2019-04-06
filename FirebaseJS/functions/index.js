const functions = require('firebase-functions');

//Change the threat level status to 0
function threatLevel0(change){
   return change.ref.set({
         threatLvl: 0
       }, {merge: true});
}

//Change the threat level status to 1
function threatLevel1(change){
   return change.ref.set({
         threatLvl: 1
       }, {merge: true});
}

// Listen for creation documents in the 'Gunshots' collection
exports.startGunshotTimers = functions.firestore
    .document('Gunshots/{docID}')
    .onCreate((change, context) => {

      //change the threat level to 1 after 10 seconds
      setTimeout(threatLevel1, 10000, change);           
      
      //change the threat level to 0 after 30 seconds
      return setTimeout(threatLevel0, 30000, change);
    });
