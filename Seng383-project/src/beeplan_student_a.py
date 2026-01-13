"""
BeePlan - Student A
Algorithm + Clean Coding + Error Handling
-----------------------------------------
Simple backtracking scheduler for Çankaya University course scheduling.
"""

from dataclasses import dataclass
from typing import List

# ----------------- Global constants ----------------- #

DAYS = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"]
HOURS = [9, 10, 11, 12, 13, 14, 15, 16]  # 13,14 = Friday exam block (no course)


# ----------------- Data models ----------------- #

@dataclass(frozen=True)
class TimeSlot:
    day: str
    hour: int


@dataclass
class Course:
    code: str
    name: str
    year: int
    theory_hours: int
    lab_hours: int
    is_elective: bool
    instructor: str
    expected_students: int


@dataclass
class Classroom:
    room_id: str
    room_type: str  # "lab" or "class"
    capacity: int


@dataclass
class Assignment:
    course_code: str
    kind: str  # "theory" or "lab"
    timeslot: TimeSlot
    room_id: str
    instructor: str
    year: int
    is_elective: bool


class SchedulingError(Exception):
    """Custom exception for BeePlan scheduling failures."""
    pass


# ----------------- Scheduler core (Student A) ----------------- #

class BeePlanScheduler:
    """
    Simple backtracking scheduler implementation for BeePlan.
    Student A is responsible for this algorithm, clean coding,
    and error handling.
    """

    def __init__(self,
                 courses: List[Course],
                 classrooms: List[Classroom]):
        self.courses = courses
        self.classrooms = classrooms
        self.assignments: List[Assignment] = []

        # Precompute all possible timeslots (except Friday exam block hours)
        self.timeslots: List[TimeSlot] = []
        for day in DAYS:
            for h in HOURS:
                # Friday 13:20–15:10 exam block → no courses allowed
                if day == "Friday" and h in (13, 14):
                    continue
                self.timeslots.append(TimeSlot(day, h))

    # ---------------- Constraint helpers ---------------- #

    def _instructor_daily_theory_hours(self,
                                       instructor: str,
                                       day: str) -> int:
        """Total theory hours assigned to an instructor in a single day."""
        hours = 0
        for a in self.assignments:
            if a.instructor == instructor and a.kind == "theory" and a.timeslot.day == day:
                hours += 1
        return hours

    def _year_has_course_at(self, year: int, timeslot: TimeSlot) -> bool:
        """Check if the given year already has a course at this timeslot."""
        return any(a.year == year and a.timeslot == timeslot
                   for a in self.assignments)

    def _elective_conflict(self,
                           is_elective: bool,
                           year: int,
                           timeslot: TimeSlot) -> bool:
        """
        Simple elective rule:
        - Avoid overlapping electives with each other.
        - Avoid overlapping electives with 3rd-year compulsory courses.
        """
        for a in self.assignments:
            if a.timeslot != timeslot:
                continue
            # elective with elective
            if is_elective and a.is_elective:
                return True
            # any elective with 3rd year compulsory
            if is_elective and a.year == 3 and not a.is_elective:
                return True
            if year == 3 and not is_elective and a.is_elective:
                return True
        return False

    def _room_available(self, room_id: str, timeslot: TimeSlot) -> bool:
        """Check if a room is free at a given timeslot."""
        return not any(a.room_id == room_id and a.timeslot == timeslot
                       for a in self.assignments)

    def _lab_after_theory_ok(self,
                             course: Course,
                             candidate: Assignment) -> bool:
        """
        Ensure lab sessions are scheduled after theory sessions.

        For simplicity:
        - theory and lab may be on different days,
        - but lab cannot start earlier in the week/day
          than the first theory hour.
        """
        if candidate.kind == "theory" or course.lab_hours == 0:
            return True

        # candidate is a lab
        theory_slots = [a.timeslot for a in self.assignments
                        if a.course_code == course.code and a.kind == "theory"]
        if not theory_slots:
            # If no theory placed yet, allow temporarily; later assignments will still be checked
            return True

        earliest = min(
            theory_slots,
            key=lambda ts: (DAYS.index(ts.day), ts.hour)
        )
        cand_key = (DAYS.index(candidate.timeslot.day), candidate.timeslot.hour)
        earliest_key = (DAYS.index(earliest.day), earliest.hour)
        return cand_key >= earliest_key

    def _capacity_ok(self, classroom: Classroom, course: Course) -> bool:
        """
        Capacity rules:
        - General capacity must be enough for expected students.
        - Lab sections must not exceed 40 students.
        """
        if classroom.room_type == "lab":
            return (
                classroom.capacity >= course.expected_students
                and classroom.capacity <= 40
            )
        return classroom.capacity >= course.expected_students

    def _check_constraints(self,
                           course: Course,
                           kind: str,
                           timeslot: TimeSlot,
                           classroom: Classroom) -> bool:
        """
        Central constraint checker for a single (course, kind, timeslot, room)
        candidate assignment.
        """

        # 1) Instructor daily theory limit (max 4 hours)
        if kind == "theory":
            theory_hours = self._instructor_daily_theory_hours(
                course.instructor,
                timeslot.day
            )
            if theory_hours >= 4:
                return False

        # 2) Year: no two courses at the same time
        if self._year_has_course_at(course.year, timeslot):
            return False

        # 3) Elective and 3rd-year compatibility
        if self._elective_conflict(course.is_elective, course.year, timeslot):
            return False

        # 4) Room availability
        if not self._room_available(classroom.room_id, timeslot):
            return False

        # 5) Capacity & lab rules
        if not self._capacity_ok(classroom, course):
            return False

        # 6) Lab after theory
        candidate_assignment = Assignment(
            course_code=course.code,
            kind=kind,
            timeslot=timeslot,
            room_id=classroom.room_id,
            instructor=course.instructor,
            year=course.year,
            is_elective=course.is_elective
        )
        if not self._lab_after_theory_ok(course, candidate_assignment):
            return False

        return True

    # ---------------- Backtracking scheduler ---------------- #

    def build_tasks(self) -> List[tuple]:
        """
        Expand each course into (course, kind) pairs,
        repeating according to the number of weekly hours.
        Sorted as: lower year first, theory before lab.
        """
        tasks: List[tuple] = []
        for c in self.courses:
            for _ in range(c.theory_hours):
                tasks.append((c, "theory"))
            for _ in range(c.lab_hours):
                tasks.append((c, "lab"))

        tasks.sort(key=lambda t: (t[0].year, 0 if t[1] == "theory" else 1))
        return tasks

    def schedule(self) -> List[Assignment]:
        """
        Run a backtracking search to place all (course, kind) tasks
        into (TimeSlot, Classroom) pairs under the constraints.
        """
        tasks = self.build_tasks()

        def backtrack(index: int) -> bool:
            if index == len(tasks):
                return True  # all tasks placed

            course, kind = tasks[index]

            for ts in self.timeslots:
                for room in self.classrooms:
                    if kind == "lab" and room.room_type != "lab":
                        continue
                    if kind == "theory" and room.room_type not in ("class", "lab"):
                        continue

                    if self._check_constraints(course, kind, ts, room):
                        self.assignments.append(
                            Assignment(
                                course_code=course.code,
                                kind=kind,
                                timeslot=ts,
                                room_id=room.room_id,
                                instructor=course.instructor,
                                year=course.year,
                                is_elective=course.is_elective
                            )
                        )
                        if backtrack(index + 1):
                            return True
                        self.assignments.pop()

            return False

        if not backtrack(0):
            raise SchedulingError("No valid schedule could be generated.")

        return self.assignments


# -------------- Sample data & helper for GUI / CLI -------------- #

def build_sample_scheduler() -> BeePlanScheduler:
    """
    Build a small scheduler instance with example data so that
    both Student A (CLI) and Student B (GUI) can test the algorithm.
    """

    courses = [
        Course("SENG201", "Data Structures", 2, theory_hours=3, lab_hours=1,
               is_elective=False, instructor="Dr. A", expected_students=40),
        Course("SENG301", "Software Project Management", 3, theory_hours=2, lab_hours=1,
               is_elective=False, instructor="Dr. B", expected_students=45),
        Course("SENG383", "Software Project III", 3, theory_hours=0, lab_hours=2,
               is_elective=False, instructor="Dr. C", expected_students=40),
        Course("ELEC474", "Human Computer Interaction", 3, theory_hours=3, lab_hours=0,
               is_elective=True, instructor="Dr. D", expected_students=30),
    ]

    classrooms = [
        Classroom("C101", "class", 70),
        Classroom("C102", "class", 50),
        Classroom("LAB1", "lab", 40),
        Classroom("LAB2", "lab", 40),
    ]

    return BeePlanScheduler(courses, classrooms)


if __name__ == "__main__":
    scheduler = build_sample_scheduler()
    assignments = scheduler.schedule()
    print("Generated schedule:")
    for a in assignments:
        print(f"{a.course_code:8s} | {a.kind:6s} | "
              f"{a.timeslot.day:9s} {a.timeslot.hour}:00 | {a.room_id}")
