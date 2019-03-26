DocumentReference docRef = db.collection("employees").doc("JdkK...");
docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
    @Override
    public void onSuccess(DocumentSnapshot documentSnapshot) {
        Map<String, Object> forms = documentSnapshot.get("dynForms");
        for (Map.Entry<Object, Object> form: forms.entrySet()) {
            String key = (String) form.getKey();
            Map<Object, Object> values = (Map<Object, Object>)form.getValues();
            String name = (String) values.get("formName");
        }
    }
})