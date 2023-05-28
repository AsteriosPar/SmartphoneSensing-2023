#open KNN Database file
import sqlite3

class DatabaseClass(sqlite3.SQLiteOpenHelper):
    DATABASE_NAME = "Bayesian_Database"
    AP = "access_points"

    def __init__(self, context):
        super().__init__(context, DatabaseClass.DATABASE_NAME, None, 1)

    def onCreate(self, db):
        # Add table that stores the measurements of each cell
        db.execute("CREATE TABLE MEASUREMENTS (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, COLUMNS INTEGER)")

        for i in range(1, 21):
            query = f"CREATE TABLE C{i} (ID INTEGER PRIMARY KEY AUTOINCREMENT, {DatabaseClass.AP} TEXT"
            for j in range(1, 41):
                query += f", M{j} INTEGER"
            query += ")"
            db.execute(query)

            contentValues = {
                "NAME": f"C{i}",
                "COLUMNS": 0
            }
            db.execute("INSERT INTO MEASUREMENTS (NAME, COLUMNS) VALUES (:NAME, :COLUMNS)", contentValues)

    def onUpgrade(self, db, oldVersion, newVersion):
        for j in range(1, 21):
            db.execute(f"DROP TABLE IF EXISTS C{j}")
        self.onCreate(db)

    def deleteAllData(self):
        db = self.getWritableDatabase()
        for j in range(17, 21):
            db.execute(f"DELETE FROM C{j}")
            query = f"UPDATE MEASUREMENTS SET COLUMNS = '0' WHERE NAME = 'C{j}'"
            db.execute(query)

    def checkAPExists(self, access_point, cell):
        db = self.getReadableDatabase()
        st = f"SELECT * FROM {cell} WHERE {DatabaseClass.AP} = '{access_point}'"
        data = db.execute(st)
        return data.fetchone() is not None

    def addData(self, ar, signal_strength, cell, cur_column):
        db = self.getWritableDatabase()
        if not self.checkAPExists(ar, cell):
            contentValues = {
                DatabaseClass.AP: ar,
                cur_column: signal_strength
            }
            result = db.execute(f"INSERT INTO {cell} ({DatabaseClass.AP}, {cur_column}) VALUES (:access_point, :signal_strength)", contentValues)
            return result.lastrowid != -1
        else:
            query = f"UPDATE {cell} SET {cur_column} = '{signal_strength}' WHERE {DatabaseClass.AP} = '{ar}'"
            db.execute(query)
        return True

    def getData(self, ar, cell, measurement):
        db = self.getWritableDatabase()
        cursor = db.execute(f"SELECT {measurement} FROM {cell} WHERE {DatabaseClass.AP} = '{ar}'")
        row = cursor.fetchone()
        if row is not None:
            return row[0]
        return 1

    def isNull(self, access_point, cell, measurement):
        db = self.getWritableDatabase()
        cursor = db.execute(f"SELECT {measurement} FROM {cell} WHERE {DatabaseClass.AP} = '{access_point}'")
        row = cursor.fetchone()
        if row is not None:
            return row[0] is None
        return False

    def getPopulatedColumns(self, cell):
        db = self.getWritableDatabase()
        cursor = db.execute(f"SELECT COLUMNS FROM MEASUREMENTS WHERE NAME = '{cell}'")
        row = cursor.fetchone()
        if row is not None:
            return row[0]
        return 0

    def increaseColumnCount(self, cell):
        db = self.getWritableDatabase()
        cursor = db.execute(f"SELECT COLUMNS FROM MEASUREMENTS WHERE NAME = '{cell}'")
        row = cursor.fetchone()
        if row is not None:
            index = row[0] + 1
            if index < 40:
                query = f"UPDATE MEASUREMENTS SET COLUMNS = '{index}' WHERE NAME = '{cell}'"
                db.execute(query)
            else:
                query = f"UPDATE MEASUREMENTS SET COLUMNS = '1' WHERE NAME = '{cell}'"
                db.execute(query)

