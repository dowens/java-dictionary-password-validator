# Sample Usage #

## Password Validation ##
```
 DictionaryPasswordValidator pDV = DictionaryPasswordValidator.getInstance();
 if(pDV.isPasswordDictionaryBased("Pa8!ss9wo4rd2") == true) {
     System.out.println("Password contains a dictionary word!");
 } else {
     System.out.println("Password is valid.");
 }
```

## Dictionary Look up ##
```
 DictionaryPasswordValidator pDV = DictionaryPasswordValidator.getInstance();
 pDV.isDictionaryWord("word");
```



## To Modify The Default Configuration ##

To configure your own options, this method has to be called prior to any getInstance() calls.  So, if you're developing a web app (for example), you should call this when your servlet container starts up.  It's default is a min word length of 4.  Accuracy is already set to where the current data set is optimal (17).  You can play with it however you choose though.
```
    DictionaryPasswordValidator.configure(accuracy, minWordCharLength);
```