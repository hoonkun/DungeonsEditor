import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat

@Serializable
data class GitCommit(
    @SerialName("abbreviated_commit")
    val abbreviatedCommit: String,
    val subject: String,
    val body: String,
    val author: GitCommitAuthor
)

@Serializable
data class GitCommitAuthor(
    val date: String,
    val name: String
)

class GitCommits {
    companion object {
        val current: List<GitCommit> = Json.decodeFromString({}::class.java.getResource("applied_git_logs.json")!!.readText())

        fun transformDate(date: String): String {
            val parser = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss X")
            val formatter = SimpleDateFormat("yy:MM:dd HH:mm")
            return formatter.format(parser.parse(date))
        }
    }
}