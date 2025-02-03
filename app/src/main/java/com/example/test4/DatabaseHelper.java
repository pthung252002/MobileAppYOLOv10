package com.example.test4;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.ContentValues;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "DISEAS.db";
    private static final int DATABASE_VERSION = 3;  // Thay đổi số phiên bản

    // Bảng diseases
    public static final String TABLE_DISEASES = "diseases";
    public static final String COLUMN_D_ID = "d_id";
    public static final String COLUMN_D_NAME = "d_name";
    public static final String COLUMN_D_DESCRIPTION = "d_description";

    // Bảng cause (không có khóa ngoại nữa)
    public static final String TABLE_CAUSE = "cause";
    public static final String COLUMN_C_ID = "c_id";
    public static final String COLUMN_C_DES = "c_des";
    public static final String COLUMN_C_D_ID = "d_id"; // Cột thông thường thay vì khóa ngoại

    // Bảng prevention (không có khóa ngoại nữa)
    public static final String TABLE_PREVENT = "prevent";
    public static final String COLUMN_P_ID = "P_id";
    public static final String COLUMN_P_DES = "P_des";
    public static final String COLUMN_P_D_ID = "d_id"; // Cột thông thường thay vì khóa ngoại

    // Bảng symptom (không có khóa ngoại nữa)
    public static final String TABLE_SYMPTOMS = "symptom";
    public static final String COLUMN_S_ID = "s_id";
    public static final String COLUMN_S_DES = "s_des";
    public static final String COLUMN_S_D_ID = "d_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng diseases
        String CREATE_DISEASES_TABLE = "CREATE TABLE " + TABLE_DISEASES + " (" +
                COLUMN_D_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_D_NAME + " TEXT, " +
                COLUMN_D_DESCRIPTION + " TEXT)";
        db.execSQL(CREATE_DISEASES_TABLE);

        // Tạo bảng cause (không có khóa ngoại)
        String CREATE_CAUSE_TABLE = "CREATE TABLE " + TABLE_CAUSE + " (" +
                COLUMN_C_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_C_DES + " TEXT, " +
                COLUMN_C_D_ID + " INTERGER)";
        db.execSQL(CREATE_CAUSE_TABLE);

        // Tạo bảng prevent (không có khóa ngoại)
        String CREATE_PREVENT_TABLE = "CREATE TABLE " + TABLE_PREVENT + " (" +
                COLUMN_P_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_P_DES + " TEXT, " +
                COLUMN_P_D_ID + " INTERGER)";
        db.execSQL(CREATE_PREVENT_TABLE);

        // Tạo bảng symptom (không có khóa ngoại)
        String CREATE_STMPTOM_TABLE = "CREATE TABLE "+ TABLE_SYMPTOMS + " (" +
                COLUMN_S_ID + " INTERGER PRIMARY KEY, " +
                COLUMN_S_DES + " TEXT, " +
                COLUMN_S_D_ID + " INTERGER)";
        db.execSQL(CREATE_STMPTOM_TABLE);

        // Thêm dữ liệu mẫu
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CAUSE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREVENT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SYMPTOMS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISEASES);
            onCreate(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        insertDiseaseInfo(db, 0, "Early blight", "Early blight is a common fungal disease that primarily affects tomato plants, including their leaves, stems, and fruits. It is caused by the fungus Alternaria solani and can result in significant yield losses if not properly managed. The disease is most prevalent in warm and humid conditions.");
        insertDiseaseInfo(db, 1, "Healthy", "Healthy leaf");
        insertDiseaseInfo(db, 2, "Late blight", "Late blight is a devastating disease of tomato plants caused by the water mold Phytophthora infestans. It primarily affects leaves, stems, and fruits, leading to rapid plant decline under favorable conditions. Late blight is often associated with cool, wet weather and can spread quickly, resulting in severe crop losses.");
        insertDiseaseInfo(db, 3, "Leaf mold", "Leaf mold is a fungal disease affecting tomato leaves, caused by Cladosporium fulvum (syn. Passalora fulva). It thrives in humid, poorly ventilated environments, reducing plant vigor and yield by impairing photosynthesis. While it primarily affects foliage, severe infections can indirectly impact fruit production.");
        insertDiseaseInfo(db, 4, "Yellow leaf curl", "Yellow leaf curl is a viral disease affecting tomato plants, caused by the Tomato yellow leaf curl virus (TYLCV). It is transmitted by whiteflies (Bemisia tabaci) and results in stunted growth, reduced yields, and poor-quality fruits. The virus primarily targets the leaves, causing deformation and discoloration.");

        // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // Symptom
        // Symptom - EB
        insertSymptom(db, 0, "Small, dark spots on older leaves that enlarge to form circular lesions with concentric rings, giving a 'target spot' appearance.", 0);
        insertSymptom(db, 1, "Yellowing and wilting of leaves, especially lower leaves, as the infection progresses.", 0);
        insertSymptom(db, 2, "Premature defoliation of the plant, reducing photosynthetic capacity and exposing fruits to sunscald.", 0);

        // Symptom - LB
        insertSymptom(db, 3, "Water-soaked lesions on leaves that rapidly enlarge and turn dark brown or black.", 2);
        insertSymptom(db, 4, "A white, powdery fungal growth on the underside of infected leaves under high humidity.", 2);
        insertSymptom(db, 5, "Entire leaflets or leaves dying and shriveling, often progressing from the lower part of the plant upward.", 2);
        insertSymptom(db, 6, "Stems developing dark, greasy-looking streaks.", 2);
        insertSymptom(db, 7, "Fruits showing firm, brown, and leathery spots, often with a sunken appearance.", 2);

        // Symptom - LM
        insertSymptom(db, 8, "Pale green or yellow spots on the upper surface of older leaves.", 3);
        insertSymptom(db, 9, "Olive-green or brown velvety mold growth on the underside of infected leaves.", 3);
        insertSymptom(db, 10, "Leaves curling, wilting, and eventually dropping off as the infection progresses.", 3);
        insertSymptom(db, 11, "In severe cases, defoliation exposes fruits to sunscald and may reduce fruit quality.", 3);

        // Symptom - CV
        insertSymptom(db, 12, "Upward curling of leaves, often with crinkled or misshapen leaf edges.", 4);
        insertSymptom(db, 13, "Interveinal yellowing or complete chlorosis of leaves while veins remain green.", 4);
        insertSymptom(db, 14, "Stunted plant growth and reduced internode length, giving plants a bushy appearance.", 4);
        insertSymptom(db, 15, "Reduced flowering and fruit set, leading to decreased yields.", 4);


        // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // Cause
        // Cause - EB
        insertCause(db, 0,"Infection by the Alternaria solani fungus, which survives in soil, plant debris, or seeds.", 0);
        insertCause(db, 1,"High humidity and warm temperatures that favor fungal growth and spore dissemination.", 0);
        insertCause(db, 2,"Poor plant care practices, such as overhead watering, which increases leaf wetness duration.", 0);

        // Cause - LB
        insertCause(db, 3, "Infection by Phytophthora infestans, which spreads via wind-dispersed spores and water splashes.", 2);
        insertCause(db, 4, "Prolonged periods of cool, wet, or humid weather, which favor spore germination and infection.", 2);
        insertCause(db, 5, "Infected plant debris, cull piles, or volunteer plants harboring the pathogen.", 2);
        insertCause(db, 6, "Lack of crop rotation or planting susceptible tomato varieties.", 2);
        insertCause(db, 7, "Lack of crop rotation or planting susceptible tomato varieties.", 2);

        // Cause - LM
        insertCause(db, 8, "Infection by Passalora fulva, which spreads via airborne spores or water splashes.", 3);
        insertCause(db, 9, "Prolonged periods of high humidity, low air circulation, and moderate temperatures.", 3);
        insertCause(db, 10, "Overcrowding of plants, leading to limited airflow and increased leaf wetness duration.", 3);
        insertCause(db, 11, "Contaminated seeds or plant debris harboring fungal spores.", 3);

        // Cause - CV
        insertCause(db, 12, "Infection by the Tomato yellow leaf curl virus (TYLCV), which is transmitted by whiteflies.", 4);
        insertCause(db, 13, "High populations of whiteflies, especially in warm and dry climates.", 4);
        insertCause(db, 14, "Movement of infected transplants or cuttings carrying the virus.", 4);
        insertCause(db, 15, "Continuous cropping of susceptible plants in the same area without breaks.", 4);


        // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // Prevent
        insertPrevent(db, 0, "Use disease-resistant tomato varieties and certified disease-free seeds.", 0);
        insertPrevent(db, 1, "Practice crop rotation, avoiding planting tomatoes in the same soil for consecutive seasons.", 0);
        insertPrevent(db, 2, "Apply fungicides preventively, especially during periods of high humidity, and follow integrated pest management strategies.", 0);

        // Prevent - LB
        insertPrevent(db, 3, "Plant resistant tomato varieties that are less susceptible to late blight.", 2);
        insertPrevent(db, 4, "Ensure proper spacing between plants to improve air circulation and reduce leaf wetness duration.", 2);
        insertPrevent(db, 5, "Avoid overhead watering to minimize water on foliage.", 2);
        insertPrevent(db, 6, "Regularly monitor plants and remove any infected leaves or plants promptly to prevent disease spread.", 2);
        insertPrevent(db, 7, "Use preventative fungicides, especially during wet weather, and rotate fungicides to reduce resistance development.", 2);
        insertPrevent(db, 8, "Practice good sanitation by removing plant debris and destroying infected plants at the end of the season.", 2);

        // Prevent - LM
        insertPrevent(db, 9, "Use resistant tomato varieties, where available, to reduce susceptibility to leaf mold.", 3);
        insertPrevent(db, 10, "Ensure adequate spacing between plants to improve air circulation and reduce humidity.", 3);
        insertPrevent(db, 11, "Water plants at the base instead of overhead to keep leaves dry.", 3);
        insertPrevent(db, 12, "Regularly prune and remove lower leaves to minimize disease spread and enhance airflow.", 3);
        insertPrevent(db, 13, "Apply preventive fungicides in high-risk conditions, and rotate chemical classes to prevent resistance.", 3);
        insertPrevent(db, 14, "Practice good sanitation by removing and destroying infected plant material after the growing season.", 3);

        // Prevent - CV
        insertPrevent(db, 15, "Use TYLCV-resistant tomato varieties to minimize disease impact.", 4);
        insertPrevent(db, 16, "Control whitefly populations using insecticides, biological controls (e.g., predatory insects), or yellow sticky traps.", 4);
        insertPrevent(db, 17, "Cover plants with fine mesh or netting to exclude whiteflies and reduce infection risk.", 4);
        insertPrevent(db, 18, "Remove and destroy infected plants immediately to prevent the virus from spreading.", 4);
        insertPrevent(db, 19, "Rotate crops with non-host plants to disrupt the whitefly lifecycle and reduce virus pressure.", 4);
        insertPrevent(db, 20, "Maintain good field hygiene by removing weeds that serve as alternate hosts for whiteflies and the virus.", 4);

    }

    private void insertDiseaseInfo(SQLiteDatabase db, int id, String name, String description) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_D_ID, id);
        values.put(COLUMN_D_NAME, name);
        values.put(COLUMN_D_DESCRIPTION, description);
        db.insert(TABLE_DISEASES, null, values);
    }

    // Thêm phương thức để chèn dữ liệu vào bảng cause
    public void insertCause(SQLiteDatabase db, int causeId, String description, int diseaseId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_C_ID, causeId);
        values.put(COLUMN_C_DES, description);
        values.put(COLUMN_C_D_ID, diseaseId);
        db.insert(TABLE_CAUSE, null, values);
    }

    // Thêm phương thức để chèn dữ liệu vào bảng prevent
    public void insertPrevent(SQLiteDatabase db, int preventId, String description, int diseaseId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_P_ID, preventId);
        values.put(COLUMN_P_DES, description);
        values.put(COLUMN_P_D_ID, diseaseId);
        db.insert(TABLE_PREVENT, null, values);
    }

    // Thêm phương thức để chèn dữ liệu vào bảng symptom
    public void insertSymptom(SQLiteDatabase db, int symptomId, String description, int diseaseId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_S_ID, symptomId);
        values.put(COLUMN_S_DES, description);
        values.put(COLUMN_S_D_ID, diseaseId);
        db.insert(TABLE_SYMPTOMS, null, values);
    }

    // Lấy d_description từ bảng diseases dựa vào d_id
    public String getDiseaseDescription(int diseaseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DISEASES, new String[]{COLUMN_D_DESCRIPTION},
                COLUMN_D_ID + "=?", new String[]{String.valueOf(diseaseId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_D_DESCRIPTION));
            cursor.close();
            return description;
        }
        return null;
    }


    // Lấy danh sách các c_des từ bảng cause dựa vào d_id
    public String[] getCauseDescriptions(int diseaseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CAUSE, new String[]{COLUMN_C_DES},
                COLUMN_C_D_ID + "=?", new String[]{String.valueOf(diseaseId)},
                null, null, null);

        // Chuyển đổi dữ liệu từ Cursor thành mảng String
        String[] descriptions = null;
        if (cursor != null && cursor.getCount() > 0) {
            descriptions = new String[cursor.getCount()];
            int index = 0;
            while (cursor.moveToNext()) {
                descriptions[index++] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_C_DES));
            }
            cursor.close();
        }
        return descriptions;
    }

    // Lấy danh sách các p_des từ bảng prevent dựa vào d_id
    public String[] getPreventDescriptions(int diseaseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PREVENT, new String[]{COLUMN_P_DES},
                COLUMN_P_D_ID + "=?", new String[]{String.valueOf(diseaseId)},
                null, null, null);

        // Chuyển đổi dữ liệu từ Cursor thành mảng String
        String[] descriptions = null;
        if (cursor != null && cursor.getCount() > 0) {
            descriptions = new String[cursor.getCount()];
            int index = 0;
            while (cursor.moveToNext()) {
                descriptions[index++] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_DES));
            }
            cursor.close();
        }
        return descriptions;
    }

    // Lấy danh sách các p_des từ bảng prevent dựa vào d_id
    public String[] getSymptomDescriptions(int diseaseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SYMPTOMS, new String[]{COLUMN_S_DES},
                COLUMN_S_D_ID + "=?", new String[]{String.valueOf(diseaseId)},
                null, null, null);

        // Chuyển đổi dữ liệu từ Cursor thành mảng String
        String[] descriptions = null;
        if (cursor != null && cursor.getCount() > 0) {
            descriptions = new String[cursor.getCount()];
            int index = 0;
            while (cursor.moveToNext()) {
                descriptions[index++] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_S_DES));
            }
            cursor.close();
        }
        return descriptions;
    }
}
