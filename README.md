<p><img src="Smart_Bin_Dashboard_Logo.png"/></p>
<h1><b>Smart Bin Dashboard</b></h1>
<br>
<p>Εφαρμογή έξυπνης διαχείρισης σκουπιδιών για τον 3ο Μαθητικό και Φοιτητικό Διαγωνισμό Προγραμματισμού, με θέμα «Κοζάνη 2030: Κλιματικά ουδέτερη και έξυπνη πόλη».</p>

<h2><b>Περιγραφή Εφαρμογής</b></h2>
<br>
<p>Η εφαρμογή έξυπνης διαχείρισης κάδων δίνει στον οδηγό του απορριμματοφόρου τη δυνατότητα να γνωρίζει ποιοι είναι οι γεμάτοι κάδοι στην πόλη και ποια διαδρομή πρέπει να ακολουθήσει για να τους αδειάσει. Αυτό επιτυγχάνεται τοποθετώντας έξυπνες συσκευές στους κάδους, έτσι ώστε όταν γεμίζουν να ενημερώνεται η πλατφόρμα και να υπολογίζεται η βέλτιστη διαδρομή για να περάσει το απορριμματοφόρο να τους αδειάσει.</p>

<h2><b>Δυνατότητές Εφαρμογής</b></h2>
<br>
<p>Ο χρήστης έχει τις εξής δυνατότητές
  <ul>
    <li>Ενεργοποίηση/Απενεργοποίηση τοποθεσίας</li>
    <li>Ενεργοποίηση/Απενεργοποίηση εμφάνισης διαδρομής</li>
    <li>Ενεργοποίηση πλοήγησης</li>
    <li>Ανάκτηση δεδομένων και υπολογισμός βέλτιστης διαδρομής</li>
    <li>Πρόσθεση/Αφαίρεσή κάδου στον χάρτη</li>
  </ul>
</p>

<h2><b>Αλγόριθμος-Λογική Εφαρμογής</b></h2>
<br>
<p>Οι πληροφορίες των κάδων της πόλης, δηλαδή η τοποθεσία τους, η κατάσταση τους(γεμάτος, μη γεμάτος) και ο κωδικός τους είναι αποθηκευμένες σε μία βάση δεδομένων. Σε αυτή την εφαρμογή, σαν βάση δεδομένων χρησιμοποιείται η Firebase η οποία είναι αξιόπιστη και εύκολη στη χρήση. Η εφαρμογή χρήστη εξάγει τις πληροφορίες απο τη βάση δεδομένων και υπολογίζει τη βέλτιστη διαδρομή. Αυτό συμβαίνει όταν 
  <ul>
    <li>Tο επιλέξει ο χρήστης.
    <li>Όταν γεμίσει ένας κάδος.
  </ul>
Η αποθήκευση των πληροφοριών των κάδων στη βάση δεδομένων γίνεται σε διακριτές περιοχές εντός μιας πόλης. Αυτό πρακτικά σημαίνει ότι κάθε περιοχή έχει το δικό της σύνολο κάδων και έτσι οι κάδοι μιας περιοχής δεν μπορούν να ανήκουν σε άλλη περιοχή. Η διαχείριση των περιοχών, δηλαδή ποιοι κάδοι ανήκουν σε ποιες περιοχές είναι ζήτημα που δεν υλοποιείται σε αυτήν την εφαρμογή.</p>
<p>Ο κάθε χρήστης(οδηγός) θα πρέπει πριν αρχίσει να χρησιμοποιεί τη πλατφόρμα να δημιουργήσει ένα λογαριασμό και να συνδεθεί. Αυτό απαιτείται διότι στον κάθε χρήστη αποδίδεται ένα σύνολο από περιοχές που θα πρέπει να καλύψει.Αποδίδοντας σε κάθε χρήστη(οδηγό) ξεχωριστές περιοχές διασφαλίζουμε ότι δεν θα υπάρχει σφάλμα στα δρομολόγια, και έτσι οι κάδοι σε μια περιοχή στην οποία είναι υπεύθυνος ένας οδηγός δεν θα εμφανίζονται σε περιοχές αλλού οδηγού.</p>

<h2><b>Ενδεικτική Εκτέλεση Εφαρμογής</b></h2>
<br>
<p>Για να δείτε ένα ενδεικτικό βίντεο της εκτέλεσης της εφαρμογής πατήστε <a href="https://drive.google.com/file/d/1s6Z_FnqI7sg0LSpn9HlZQKC92GjBPlka/view?usp=sharing">εδώ</a></p>
<p>Την παρουσίαση της εφαρμογής μπορείτε να τη βρείτε <a href="https://docs.google.com/presentation/d/1arZdm2npij3xdY_feV3NMuFBHRb06pYGw1QFPU3O7Nk/edit#slide=id.g11b7600d03a_0_2616">εδώ</a></p>

<h2><b>Επιπλέον Σχόλιά</b></h2>
<br>
<p>Στο Dashboard activity της εφαρμογής μόνο το Map πεδίο είναι λειτουργικό καθώς η λογική της εφαρμογής υλοποιείται εκεί.</p>

<p>Όταν κατεβάσετε το APK της εφαρμογής να γνωρίζετε πως θα πρέπει να συνδεθείτε με τον λογαριασμό <b>testuser@gmail.com</b> και κωδικό <b>tu12345</b>. Αυτό πρέπει να το κάνετε για να λειτουργήσει σωστά η εφαρμογή διότι σε αυτόν τον λογαριασμό έχουν αποδοθεί περιοχές στη Κοζάνη που βρίσκονται κάδοι απορριμάτων. Για την σωστή εκτέλεση της εφαρμογής δείτε από το 03 μέρος το κομμάτι της εκτέλεσης στη <a href="https://docs.google.com/presentation/d/1arZdm2npij3xdY_feV3NMuFBHRb06pYGw1QFPU3O7Nk/edit#slide=id.g11b7600d03a_0_2616">παρουσίαση</a> μου όπως και το ενδεικτικό <a href="https://drive.google.com/file/d/1s6Z_FnqI7sg0LSpn9HlZQKC92GjBPlka/view?usp=sharing">βίντεο</a>. Το APK μπορείτε να το κατεβάσετε <a href="https://drive.google.com/file/d/1ohY2OGhS9HxhZz4Dvma31TOFPzT-z3bU/view?usp=sharing">εδώ</a></p>


