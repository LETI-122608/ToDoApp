# ToDoApp

-QR Code Feature
Adds the ability to generate and download QR codes for any task or custom text.
Usage
In the Task List, each row has a QR button.
→ Click it to open a dialog with the QR code for that task’s description and a Download PNG link.
In the sidebar, open QR Generator or go to http://localhost:8080/qr.
→ Enter any text or URL (e.g. https://iscte-iul.pt) and press Generate to view and download the code.
Technical notes
Uses ZXing (com.google.zxing:core and javase) from Maven Central.
Implemented in QRCodeService.java, integrated via QrView.java and the QR column in TaskListView.java.
Fully integrated with the Vaadin sidebar and theme.
