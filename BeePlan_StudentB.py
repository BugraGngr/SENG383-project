"""
BeePlan - Student B
GUI + Data Structures (PyQt5)
-----------------------------
Simple GUI that calls BeePlan Student A scheduler.
"""

import sys
from typing import List

from PyQt5.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QPushButton, QTableWidget, QTableWidgetItem, QLabel, QMessageBox, QTabWidget
)
from PyQt5.QtCore import Qt

import beeplan_student_a as scheduler_core  # rename this file to beeplan_student_a.py or adjust import


class BeePlanMainWindow(QMainWindow):
    """
    Simple PyQt5 GUI for BeePlan.

    Student B focuses on:
    - GUI implementation
    - Basic data presentation
    and calls the scheduling core implemented by Student A.
    """

    def __init__(self):
        super().__init__()
        self.setWindowTitle("BeePlan - Çankaya University Course Scheduling System")
        self.resize(900, 600)

        # Use sample scheduler from Student A core
        self.scheduler = scheduler_core.build_sample_scheduler()
        self.assignments: List[scheduler_core.Assignment] = []

        self._init_ui()

    def _init_ui(self):
        central = QWidget()
        main_layout = QVBoxLayout(central)

        # -------- Top info label -------- #
        self.info_label = QLabel(
            "BeePlan GUI (Student B)\n"
            "Click 'Generate Schedule' to run the scheduling algorithm\n"
            "implemented by Student A."
        )
        self.info_label.setAlignment(Qt.AlignLeft)
        main_layout.addWidget(self.info_label)

        # -------- Tabs: Timetable + Raw Assignments -------- #
        self.tabs = QTabWidget()
        main_layout.addWidget(self.tabs)

        # Timetable tab
        self.timetable_widget = QTableWidget()
        self._setup_timetable_table()
        self.tabs.addTab(self.timetable_widget, "Weekly Timetable")

        # Assignments tab
        self.assignment_table = QTableWidget()
        self._setup_assignment_table()
        self.tabs.addTab(self.assignment_table, "Assignments List")

        # -------- Bottom buttons -------- #
        btn_layout = QHBoxLayout()
        self.btn_generate = QPushButton("Generate Schedule")
        self.btn_generate.clicked.connect(self.on_generate_clicked)

        self.btn_clear = QPushButton("Clear")
        self.btn_clear.clicked.connect(self.on_clear_clicked)

        btn_layout.addWidget(self.btn_generate)
        btn_layout.addWidget(self.btn_clear)
        btn_layout.addStretch()
        main_layout.addLayout(btn_layout)

        self.setCentralWidget(central)

    def _setup_timetable_table(self):
        days = scheduler_core.DAYS
        hours = scheduler_core.HOURS

        self.timetable_widget.setRowCount(len(hours))
        self.timetable_widget.setColumnCount(len(days))

        self.timetable_widget.setHorizontalHeaderLabels(days)
        self.timetable_widget.setVerticalHeaderLabels([f"{h}:00" for h in hours])

        self.timetable_widget.horizontalHeader().setStretchLastSection(True)
        self.timetable_widget.verticalHeader().setStretchLastSection(False)

    def _setup_assignment_table(self):
        headers = ["Course", "Kind", "Day", "Hour", "Room", "Instructor", "Year", "Elective?"]
        self.assignment_table.setColumnCount(len(headers))
        self.assignment_table.setHorizontalHeaderLabels(headers)
        self.assignment_table.horizontalHeader().setStretchLastSection(True)

    # -------- Button handlers -------- #

    def on_generate_clicked(self):
        """Generate schedule using Student A algorithm."""
        try:
            self.scheduler.assignments.clear()
            self.assignments = self.scheduler.schedule()
        except scheduler_core.SchedulingError as e:
            QMessageBox.critical(self, "Scheduling Error", str(e))
            return

        self.info_label.setText("Schedule generated successfully.")
        self._populate_timetable()
        self._populate_assignments_table()

    def on_clear_clicked(self):
        """Clear current schedule from GUI."""
        self.scheduler.assignments.clear()
        self.assignments = []
        self.info_label.setText(
            "Schedule cleared. Click 'Generate Schedule' to create a new one."
        )
        # Clear tables
        for r in range(self.timetable_widget.rowCount()):
            for c in range(self.timetable_widget.columnCount()):
                self.timetable_widget.setItem(r, c, QTableWidgetItem(""))
        self.assignment_table.setRowCount(0)

    # -------- Table population helpers -------- #

    def _populate_timetable(self):
        # clear first
        for r in range(self.timetable_widget.rowCount()):
            for c in range(self.timetable_widget.columnCount()):
                self.timetable_widget.setItem(r, c, QTableWidgetItem(""))

        day_to_col = {d: i for i, d in enumerate(scheduler_core.DAYS)}
        hour_to_row = {h: i for i, h in enumerate(scheduler_core.HOURS)}

        for a in self.assignments:
            col = day_to_col[a.timeslot.day]
            row = hour_to_row[a.timeslot.hour]
            existing_item = self.timetable_widget.item(row, col)
            text = f"{a.course_code}\n{a.room_id}"
            if existing_item and existing_item.text():
                text = existing_item.text() + " | " + text
            item = QTableWidgetItem(text)
            item.setTextAlignment(Qt.AlignCenter)
            self.timetable_widget.setItem(row, col, item)

    def _populate_assignments_table(self):
        self.assignment_table.setRowCount(len(self.assignments))
        for row, a in enumerate(self.assignments):
            values = [
                a.course_code,
                a.kind,
                a.timeslot.day,
                f"{a.timeslot.hour}:00",
                a.room_id,
                a.instructor,
                str(a.year),
                "Yes" if a.is_elective else "No",
            ]
            for col, val in enumerate(values):
                item = QTableWidgetItem(val)
                item.setTextAlignment(Qt.AlignCenter)
                self.assignment_table.setItem(row, col, item)


def main():
    app = QApplication(sys.argv)
    win = BeePlanMainWindow()
    win.show()
    sys.exit(app.exec_())


if __name__ == "__main__":
    main()
