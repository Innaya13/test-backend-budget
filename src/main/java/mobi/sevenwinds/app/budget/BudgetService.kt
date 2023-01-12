package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
            }

            return@transaction entity.toResponse()
        }
    }

 suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
     transaction {
         val baseQuery = BudgetTable.select { BudgetTable.year eq param.year }

         // Считаем общее количество записей (до пагинации)
         val total = baseQuery.count()

         val query = baseQuery
             .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC) // Добавляем сортировку
             .limit(param.limit, param.offset)

         val data = BudgetEntity.wrapRows(query).map { it.toResponse() }

         val sumByType = baseQuery.groupBy { it[BudgetTable.type].name }
             .mapValues { (_, records) -> records.sumOf { it[BudgetTable.amount] } }

         return@transaction BudgetYearStatsResponse(
             total = total, // Теперь total считает все записи, а не только те, что попали в лимит
             totalByType = sumByType,
             items = data
         )
     }
  }
}