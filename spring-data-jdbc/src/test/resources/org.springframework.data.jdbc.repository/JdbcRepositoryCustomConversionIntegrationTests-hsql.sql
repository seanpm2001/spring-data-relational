CREATE TABLE ENTITY_WITH_STRINGY_BIG_DECIMAL ( id IDENTITY PRIMARY KEY, Stringy_number DECIMAL(20,10), DIRECTION INTEGER);
CREATE TABLE OTHER_ENTITY ( ID IDENTITY PRIMARY KEY, CREATED DATE, ENTITY_WITH_STRINGY_BIG_DECIMAL INTEGER);

