package com.example.thecontactsapp.DatabaseRoom;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.thecontactsapp.Model.Contact;

import java.util.List;

@Dao
public interface ContactDao {


    @Query("Select * from Contacts order by LOWER(name) ASC")
    List<Contact> getContactsWoObserver();

    @Insert
    void insertContact(Contact contact);

    @Update
    void updateContact(Contact contact);

    @Query("Delete from Contacts where id = :id")
    void deleteContact(int id);

//    @Query("WITH RECURSIVE split(contact_id, phone_no, rest) AS (\n" +
//            "  SELECT id, '', phone || ',' FROM Contacts WHERE id\n" +
//            "   UNION ALL\n" +
//            "  SELECT contact_id, \n" +
//            "         substr(rest, 0, instr(rest, ',')),\n" +
//            "         substr(rest, instr(rest, ',')+1)\n" +
//            "    FROM split\n" +
//            "   WHERE rest <> '')\n" +
//            "SELECT contact_id, replace(replace(replace(phone_no,'\"',\"\"),'[',\"\"),']',\"\") as phone_no\n" +
//            "  FROM split \n" +
//            " WHERE phone_no <> '' and phone_no <> \"null\";\n")
//    Contact getPhone();

//    @Query("SELECT T1.phone,T2.phone from Contacts T1 Left join Contacts T2 On T1.phone = T2.phone")
//    void mergeContacts();
}
