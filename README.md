# Hospital Management System
SC2002 HMS. <br>

This is a year 2 computer science project as part of our OOP curriculum. We took a structured approach towards system design and leverages encapsulation and abstraction principles of Java. This project was also written with the SOLID principles in mind.
<br><br>
We have built a hospital management system that automates the management of hospital operations, including patient management, appointment scheduling, staff management, and billing.  Patients can schedule, reschedule, and cancel appointments, view medical records, and pay outstanding bills. Doctors can manage appointments, update patient records with diagnoses and treatments, and set availability. Pharmacists can manage medication inventory, submit replenishment requests, and also send invoice to patient. Administrators oversee staff, appointments, and inventory, and approve replenishment requests.

## UML Diagrams
Below are our UML Diagrams split by functional views for ease of viewing.
Please download the relevant images to view

### Class Diagram
https://github.com/kaibin157/SC2002-SCSI-Group6/blob/main/UML%20Class%20Diagram/Group%206%20Class%20Diagram.png

### Controller Package
Below we have the class diagram for the controller package <br>
This package controls the main logic behind the code<br>

![alt text](https://github.com/VigneshManiSenthilnathan/CAM/blob/main/CampManagement/lib/images/UML_Diagrams/Package_View_Class_Diagrams/Controller_package.png)

### Model Package
Below we have the class diagram for the model package <br>
This package stores all the objects and related data used for the program <br>

![alt text](https://github.com/VigneshManiSenthilnathan/CAM/blob/main/CampManagement/lib/images/UML_Diagrams/Package_View_Class_Diagrams/Model_Package.png)

### View Package
Below we have the class diagram for the view package <br>
This package is the main interface that users interact with <br>

![alt text](https://github.com/VigneshManiSenthilnathan/CAM/blob/main/CampManagement/lib/images/UML_Diagrams/Package_View_Class_Diagrams/View_Package.png)

### Focusing on functional classes
We now narrow down our focus to the main classes that drive the program

#### Camp Controller
We see the camp controller class in action by redirecting all method calls to the correct class <br>
Any call to read, delete, view or update camps must go through this class <br>

![alt text](https://github.com/VigneshManiSenthilnathan/CAM/blob/main/CampManagement/lib/images/UML_Diagrams/Package_View_Class_Diagrams/Functional_View_Class_Diagrams/Camp_Controller.png)

#### Enquiry Controller
Next, we have the enquiry controller that manages everything enquiries related <br>

![alt text](https://github.com/VigneshManiSenthilnathan/CAM/blob/main/CampManagement/lib/images/UML_Diagrams/Package_View_Class_Diagrams/Functional_View_Class_Diagrams/Enquiry_Controller.png)

#### Manage Credentials
Here we see how we manage usernames and passwords  <br>
This class covers reading, updating, hashing and storing credentials <br>

![alt text](https://github.com/VigneshManiSenthilnathan/CAM/blob/main/CampManagement/lib/images/UML_Diagrams/Package_View_Class_Diagrams/Functional_View_Class_Diagrams/Manage_Credentials.png)

#### Report Controller
These classes are what staff and camp committee members can use to generate reports for their camps <br>

![alt text](https://github.com/VigneshManiSenthilnathan/CAM/blob/main/CampManagement/lib/images/UML_Diagrams/Package_View_Class_Diagrams/Functional_View_Class_Diagrams/Report_Controller.png)

#### Suggestions Controller
Similar to the enquiry controller, the suggestions controller manages everything to do with suggestions <br>

![alt text](https://github.com/VigneshManiSenthilnathan/CAM/blob/main/CampManagement/lib/images/UML_Diagrams/Package_View_Class_Diagrams/Functional_View_Class_Diagrams/Suggestion_Controller.png)

#### Upload and Download
These classes are responsible for reading and updating the Excel database <br>
All the other controllers (less report and manage credentials) will use data downloaded from excel and upload their data onto the excel <br>

![alt text](https://github.com/VigneshManiSenthilnathan/CAM/blob/main/CampManagement/lib/images/UML_Diagrams/Package_View_Class_Diagrams/Functional_View_Class_Diagrams/Upload_and_Download.png)

## Dependencies
We use apache.poi for our excel database. The below folder contains all necessary dependencies to be imported <br>
https://github.com/kaibin157/SC2002-SCSI-Group6/tree/main/poi-bin-5.2.3

## Collaborators
CHONG KAI BIN <br> https://github.com/kaibin157 <br><br>
LEE MEI TING <br> https://github.com/Infernoexe <br><br>
HENG ZENG XI <br> https://github.com/HengZengXi <br><br>
NG YI XIANG VIGNESH <br> https://github.com/yixiangn <br><br>
SACHDEV GARV <br> https://github.com/gavkujo <br><br>




