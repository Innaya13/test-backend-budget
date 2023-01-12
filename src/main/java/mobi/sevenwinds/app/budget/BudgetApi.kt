package mobi.sevenwinds.app.budget

import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.annotations.type.number.integer.max.Max
import com.papsign.ktor.openapigen.annotations.type.number.integer.min.Min
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import java.time.LocalDateTime

fun NormalOpenAPIRoute.budget() {
    route("/budget") {
        route("/add").post<Unit, BudgetRecord, BudgetRecord>(info("Добавить запись в бюджет")) { param, body ->
            respond(BudgetService.addRecord(body))
        }

        // Новый маршрут для добавления автора
        route("/author/add").post<Unit, Author, Author>(info("Добавить автора записи")) { param, body ->
            respond(AuthorService.addAuthor(body))
        }
    }
}

fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse {
    val authorFilter = param.authorFullName?.toLowerCase()  // Фильтр по ФИО (игнорируя регистр)

    val records = BudgetRepository.getRecordsByYear(param.year, param.limit, param.offset, authorFilter)

    val items = records.map { record ->
        BudgetRecordWithAuthor(
            year = record.year,
            month = record.month,
            amount = record.amount,
            type = record.type,
            authorFullName = record.author?.fullName,
            authorCreatedAt = record.author?.createdAt
        )
    }

    return BudgetYearStatsResponse(
        total = records.size,
        totalByType = records.groupBy { it.type }.mapValues { it.value.sumOf { record -> record.amount } },
        items = items
    )
}


data class BudgetRecord(
    @Min(1900) val year: Int,
    @Min(1) @Max(12) val month: Int,
    @Min(1) val amount: Int,
    val type: BudgetType,
    val authorId: Int? = null  // Опциональная привязка к автору
)


// Параметры для получения статистики за год
data class BudgetYearParam(
    @PathParam("Год") val year: Int,
    @QueryParam("Лимит пагинации") val limit: Int,
    @QueryParam("Смещение пагинации") val offset: Int,
)

// Ответ для статистики по году
class BudgetYearStatsResponse(
    val total: Int,
    val totalByType: Map<String, Int>,
    val items: List<BudgetRecordWithAuthor>
)

data class BudgetRecordWithAuthor(
    val year: Int,
    val month: Int,
    val amount: Int,
    val type: BudgetType,
    val authorFullName: String?,  // ФИО автора (если указан)
    val authorCreatedAt: LocalDateTime?  // Дата создания записи автора
)


// Перечисление типов бюджета
enum class BudgetType {
    Приход, Расход
}

data class Author(
    val fullName: String,  // ФИО автора
    val createdAt: LocalDateTime = LocalDateTime.now()  // Дата создания, устанавливается сервером
)


// Сервис для работы с авторами
object AuthorService {
    // Метод для добавления нового автора
    fun addAuthor(author: Author): Author {
        // Логика добавления нового автора в базу данных
        // Здесь может быть обращение к репозиторию или базе данных для сохранения
        return author  // Возвращаем сохраненный объект (или объект с ID, если это необходимо)
    }
}
