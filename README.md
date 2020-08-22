# About
This is just a simple Repository Framework for working with MySQL.
It comes with a built-in CRUD operations that can be used with little configuration on your model layer.

**Limitations**
- It does not create or read relations
- DB settings are still hard coded
- It is simple, so do not expect much =D

# Usage

1) Run `mvn clean install`
2) Setup your MySQL DB connection at: com\github\petruki\framework\db\DatabaseFactory.java
3) Annotate your model class with @Table and @Column annotations
4) Make your model extend AbstractEntity<?>.
5) Implement the repository class that extends AbstractCrudRepository<?>

An example can be found at com\github\petruki\playground