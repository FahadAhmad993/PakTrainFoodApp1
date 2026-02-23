package com.example.paktrainfoodapp.ui.main.Passenger;

public class DeliveredOrdersFragment extends ActiveOrdersFragment {
    @Override
    public void onResume() {
        super.onResume();
        try {
            java.lang.reflect.Method m = ActiveOrdersFragment.class.getDeclaredMethod("loadOrders", String.class);
            m.setAccessible(true);
            m.invoke(this, "Delivered");
        } catch (Exception ignored) {}
    }
}
