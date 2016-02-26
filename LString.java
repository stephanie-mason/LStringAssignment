/* Stephanie Mason
*/

public class LString {

  private class node {
    char data;
    node next;

    public node(char newdata) {
      data = newdata;
    }

    public node(char newdata, node newnext) {
      data = newdata;
      next = newnext;
    }
  }

  int length;
  node front;

  public LString() {
    //construct an LString object, which will represent an empty list of chars
    front = null;
  }

  //Construct LString object that is a copy of a String
  public LString(String original) {
    if (original.length() > 0) {
      front = new node(original.charAt(0));
      node curr = front;
      System.out.println("data" + curr.data);
      length++;
      //System.out.println(curr.data);
      for (int i = 1; i < original.length(); i++) {
        curr.next = new node(original.charAt(i));
        System.out.println("next data " + curr.next.data);
        length++;
      }
    } else {
      //throw length error?
    }
  }

  //return length of LString
  public int length() {
    return length;
  }

  //Create and return ordinary String with same contents as LString
  // you MAY use string and/or StringBuilder in this method only
  public String toString() {
    String result = "";
    node curr = front;
    while (curr != null) {
      result += curr.data;
      curr = curr.next;
    }
  return result;
  }

  public int compareTo(LString anotherLString) {
    // all comparisons lexicographical, ie B < BB < Ba < a
    // use compareTo method??
    if (/*this.LString == anotherLString*/true) {
      return 0;
    } else if (/*this.Lstring < anotherLString*/true) {
      return -1;
    } else if (/*this.LString > anotherLString*/false) {
      return 1;
    }

    return 0;
  }

  @Override
  public boolean equals(Object other) {
    /*
    if (other == null || !(other instance of LString)) {
    return false;
  } else {
  Lstring otherLstring = (LString)other;
  //logic here to compare this and other LString
  //return true if equal ,false else
}*/

return false;
}

public char charAt(int index) {
  /*
  char @ index
  throws IndexOutOfBoundsException
  */
  return 'a';
}

public void setCharAt(int index, char ch) {
  //set char at given index in this LString to ch
  /*
  if (index < 0 || index >= this.LString.length()) {
  throws IndexOutOfBoundsException;
}
*/

}

public LString substring(int start, int end) {
  //returns a *NEW* LString that is a substring of this LString.
  // mus tnot share any linked list nodes with old LString
  //begins at specified start and includes character at end-1
  /*
  if (start < 0 || start > end || end > this.length()) {
  throws IndexOutOfBoundsException;
}

if (start == end && end == this.length()) {
return null;
}
*/

return null;

}

public LString replace(int start, int end, LString lStr) {
  //replaces this character in a substring of this LString with characters in lStr
  //make a copy of replacement LString lStr--cannot share data
  //be sure to set length of new LString
  /*
  if (start < 0 || start > end || end > this.length()) {
  throws IndexOutOfBoundsException;
}

if (start == end) {
//insert lStr at given location in the LString
}

if (start == end && end == this.length()) {
//append lStr at end of this LString
}

//resulting LString must not share any linked list structures with lStr!!!
*/
return null;
}

}
