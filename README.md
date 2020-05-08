# The-Contact-App
Android Contacts Application.

## Features
- Store Contacts with
  - Name 
  - Multiple E-Mails
  - Multiple Tel Nos
  - Address
  - Images for the contact
- Contacts Category
  - Family
  - Home
  - Office
  - Help
  - Misc
  - Should be able to Create New Category
- All Contacts to be stored online - so less chance for data loss
- Duplicate Detection -
  - In case someone is adding information already existing it should detect duplicates for emails and phone fields.
  - It should be able to intelligently merge the contacts with similar Name using Hamming Distance.
- Back End Data Load/Store/Update
  - Database Room for Local Storage
  - MongoDB Atlas with Stich ( GraphQL ) for server side storage.
  
## Libraries Used
- Glide
- MongoDB stitch
- Apollo GraphQL
- Database Room Persistence Library
