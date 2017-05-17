package app.bryanlu.quizbowl.dbobjects;

import java.util.ArrayList;

/**
 * Created by Bryan Lu on 5/2/2017.
 *
 * Class that holds the selected status of each question category.
 */

public class CategoryList {
    private boolean biology;
    private boolean chemistry;
    private boolean geography;
    private boolean math;
    private boolean physics;
    private final String BIO_KEY = "Bio";
    private final String CHEM_KEY = "Chem&Geol";
    private final String GEO_KEY = "Geog";
    private final String MATH_KEY = "Math&CS";
    private final String PHYS_KEY = "Phys&Astro";


    public CategoryList() {
        this.biology = false;
        this.chemistry = false;
        this.geography = false;
        this.math = false;
        this.physics = false;
    }

    public CategoryList(ArrayList<String> selectedCategories) {
        if (selectedCategories.contains(BIO_KEY)) {
            this.biology = true;
        }
        if (selectedCategories.contains(CHEM_KEY)) {
            this.chemistry = true;
        }
        if (selectedCategories.contains(GEO_KEY)) {
            this.geography = true;
        }
        if (selectedCategories.contains(MATH_KEY)) {
            this.math = true;
        }
        if (selectedCategories.contains(PHYS_KEY)) {
            this.physics = true;
        }
    }

    /**
     * Creates an arraylist of keys based on the booleans of this class.
     * @return an arraylist of string keys for the selected categories
     */
    public ArrayList<String> toArrayList() {
        ArrayList<String> selectedCategories = new ArrayList<>();
        if (biology) {
            selectedCategories.add(BIO_KEY);
        }
        if (chemistry) {
            selectedCategories.add(CHEM_KEY);
        }
        if (geography) {
            selectedCategories.add(GEO_KEY);
        }
        if (math) {
            selectedCategories.add(MATH_KEY);
        }
        if (physics) {
            selectedCategories.add(PHYS_KEY);
        }
        return selectedCategories;
    }

    public boolean isBiology() {
        return biology;
    }

    public boolean isChemistry() {
        return chemistry;
    }

    public boolean isGeography() {
        return geography;
    }

    public boolean isMath() {
        return math;
    }

    public boolean isPhysics() {
        return physics;
    }
}
