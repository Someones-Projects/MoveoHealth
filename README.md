# MoveoHealth
Doctors and patients system android app

## Overview:
* The app developed with Kotlin on Android Studio IDE
* App based on the Jetpack libraries, navigation components and Material Design components. Also, can handle configuration changes like rotation and day/night theme.
* MVI (MVVM) architecture - each UI event return ViewState wrapped by his state (Loading, Success, Error).
* Use of Firebase as project backend for managing database and user authentication.

## Tools and 3rd part libraries:
* Dagger Hilt for DI.
* RecyclerView Adapter with AsyncListDiffer (animate and optimise performance for list operations)
* Coroutines and Flow for background operations inside the repository pattern.
* Observing LiveData for UI changes.
* DataStore - for saving user preferences (such as filter option)
* Process death handling - saving view state instance.
* LeakCanary - to detect memory leaks problems.
* ViewBinding - for safe and fast views reference
* Timber - Secure logging (print logs only for debug build)

## Components:
* AuthActivity: 
  * login/sign-up fragments with shared ViewModel
  * Keep the last session was connected if user hasn’t logged out:
* MainActivity:
  * 2 fragments: Patient and Doctor
  * User can switch between the 2 user types (Doctor or Patient) and will see the relevant UI corespondent.
  * Listening on Firestore db changes and update the UI  accordantly 
  * Listening task is on background thread with Flow that emits data whenever change occurred. 
  * Listening job live only when UI shown (between onSart() until onStop())
* SessionManager: Singleton object to store and mange session operation and data
