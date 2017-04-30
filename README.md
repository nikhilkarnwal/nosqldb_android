# nosqldb for android
nosqldb for android

# Features
- features of nosql database
- No static schema is required to create objectstore.
- Create multiple index in a single objecstore for efficient query
- GroupBy, OrderBy, Where Clause, Having Clause on indexed coulmns.

# Include as module
- Download this folder as zip and extract it.
- add compile project(':nosqldb') in build.gradle file of your project and sync gradle.

# Usage

# Open or Create database

    DataBase database = new new DataBase("test.db", this){
            @Override
            protected void onOpen() {
                super.onOpen();
                // callback code on database open
            }

            @Override
            protected void onCreate() {
                super.onCreate();
                // callback code on database creation
            }

            @Override
            protected void onUpgrade(int oldVersion, int newVersion) {
                super.onUpgrade(oldVersion, newVersion);
                // callback code on database upgrade
            }
        };
Create or Open objectstore

    ObjectStore objectStore = database.createObjectStore("osname");
    
Create Index according to columns to query objectstore byconditioning on these columns, ignore it if you want to fetch whole object store without any filtering always.

    ArrayList<String> idCol = new ArrayList<>();
    idCol.add("name");
    objectStore.createIndex("nameind",idCol);//pass Index name and list of columns.
    
Any number of index can be created to achieve filtering on multiple columns or set of columns.

Once all indexes are created, initiate objectstore creation using 

    objectStore.init();
    
Insert values into objectstore using 
    
    HashMap<String,String> values =  new HashMap<String, String>();

Insert 1st row with variable no. of columns

    values.put("name", "Nikhil");
    values.put("lastname", "karnwal");
    try {
        objectStore.put(values);
    } catch (Exception e) {
        e.printStackTrace();
    }
    values.clear();
Insert 2nd row with "age" as extra column but excluding "lastname" column  
    
    values.put("name", "amit");
    values.put("age", "35");
    try {
        objectStore.put(values);
    } catch (Exception e) {
        e.printStackTrace();
    }

Thus, no schema is required at the starting , all rows can have different no. of columns.

# Access data
Access data in the same way as in sqlite database by providing condition and arguments for ? in query.

    String[] args = {"amit"};
    OSCursor cursor = objectStore.query("name = ?", args);
    while (cursor.moveToNext()){
        Log.v("Value", new Gson().toJson(cursor.getRow()));
    }
    cursor.close();
Make sure that columns specified in condition are initially specified using creatIndex().

Use getCursor() to get cursor to whole objectstore.
    
    OSCursor cursor = objectStore.getCursor();
    while (cursor.moveToNext()){
        Log.v("Value", new Gson().toJson(cursor.getRow()));
    }
    cursor.close();
    
Refer to documentation for api referance.
Documentation - https://nikhilkarnwal.github.io/nosqldb_android/nosqldb/nosqldb_javadoc/index.html

Check wiki page for further Tutorial.
