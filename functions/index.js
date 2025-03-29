const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.checkUsername = functions.https.onCall(async (data, context) => {
  const username = data["username"];
  if (!username) {
    throw new functions.https.HttpsError(
        "invalid-argument",
        "El username es obligatorio",
    );
  }

  const snapshot = await admin.firestore().collection("users")
      .where("username", "==", username)
      .limit(1)
      .get();

  return {exists: !snapshot.empty};
});
