import com.grup.models.User
import com.grup.repositories.abstract.RealmUserRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

internal class TestUserRepository : RealmUserRepository() {
    private val config = RealmConfiguration.Builder(schema = setOf(User::class)).build()
    override val realm: Realm by lazy { Realm.open(config) }
}