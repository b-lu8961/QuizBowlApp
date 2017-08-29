package app.bryanlu.quizbowl.sqlite;

import java.util.ArrayList;

/**
 * Class that represents a category of questions.
 * Created by Bryan Lu on 8/25/2017.
 */

public class Category {
    private String name;
    private ArrayList<String> subcategories;

    public Category(String name) {
        this.name = name;
        subcategories = new ArrayList<>();
    }

    public void addSubcategory(String subcategory) {
        subcategories.add(subcategory);
    }

    public void removeSubcategory(String subcategory) {
        subcategories.remove(subcategory);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    ArrayList<String> getSubcategories() {
        return subcategories;
    }

    /**
     * Finds the category in the given list with the specified name.
     * @param categoryList list to look through
     * @param name category name to look for
     * @return the category with the specified name (null if not found)
     */
    public static Category findByName(ArrayList<Category> categoryList, String name) {
        for (Category category : categoryList) {
            if (category.getName().equals(name)) {
                return category;
            }
            if (category.getName().equals(name.replace(" ", ""))) {
                return category;
            }
        }
        return null;
    }

    public boolean nameEquals(String otherName) {
        String nameNoSpace = name.replace(" ", "");
        return nameNoSpace.equals(otherName.replace(" ", ""));
    }
}
