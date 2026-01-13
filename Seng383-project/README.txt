# SENG383 – Final Project  
## BeePlan & KidTask

This repository contains the final project for the **SENG383** course.  
The project consists of two applications, **BeePlan** and **KidTask**, developed to demonstrate software design, implementation, verification & validation, and responsible AI-assisted development with human-in-the-loop control.

---

##  Developer Information

- **Name:** Buğra Güngör  
- **Role:** Student A & Student B  
- **Course:** SENG383  

Both projects were developed individually, covering both frontend (GUI, data models) and backend (algorithms, validation, error handling) responsibilities.

---

##  Projects Overview

###  BeePlan – Course Scheduling System
BeePlan is a Python-based course scheduling system designed for university departments.

**Key Features:**
- Backtracking-based scheduling algorithm
- Constraint handling:
  - Friday exam block restriction
  - Instructor daily theory hour limit
  - Year-based course overlap prevention
  - Elective and 3rd-year course compatibility
  - Classroom and lab capacity limits
  - Lab-after-theory rule
- Explicit error handling using `SchedulingError`
- PyQt-based GUI for timetable visualization

**Technologies:**
- Python 3
- PyQt5

---

###  KidTask – Task & Reward Management System
KidTask is a Java-based desktop application designed to motivate children through tasks, ratings, and rewards.

**Key Features:**
- Role-based design (child, parent, teacher)
- Task completion and approval workflow
- Rating-based point system
- Level calculation based on accumulated points
- Validation and exception handling for invalid operations
- Simple Swing-based GUI

**Technologies:**
- Java
- Java Swing

---

##  AI Usage & Human-in-the-Loop

AI tools (such as Copilot, OpenAI, and Cursor-like assistants) were used during the design, coding, and testing phases to accelerate development.

However:
- AI-generated outputs were treated as **initial drafts only**
- All domain rules, constraints, validations, and error handling logic were **designed and reviewed manually**
- Critical decisions were made by the developer to ensure **correctness, robustness, and trustworthiness**

Detailed **Prompt – Output – Revision** analyses are documented in the final project report and demonstrated in the presentation video.

---

##  Verification & Validation (V&V)

Both projects were verified and validated through:
- Functional test cases
- Invalid input and error scenario testing
- AI-assisted bug discovery followed by manual fixes

Test results and V&V analysis are included in the final report.

---

##  Repository Structure

