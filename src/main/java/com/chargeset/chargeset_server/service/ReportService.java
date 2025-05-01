package com.chargeset.chargeset_server.service;

import com.chargeset.chargeset_server.document.ChargingStation;
import com.chargeset.chargeset_server.dto.reservation.ReservationInfoResponse;
import com.chargeset.chargeset_server.dto.tansaction.ChargingStat;
import com.chargeset.chargeset_server.dto.tansaction.StationStatReport;
import com.chargeset.chargeset_server.dto.tansaction.TotalStatResponse;
import com.chargeset.chargeset_server.dto.tansaction.TransactionInfoResponse;
import com.chargeset.chargeset_server.repository.charging_station.ChargingStationRepository;
import com.chargeset.chargeset_server.repository.reservation.ReservationRepository;
import com.chargeset.chargeset_server.repository.transaction.TransactionRepository;
import com.chargeset.chargeset_server.utils.SheetStyleUtils;
import com.chargeset.chargeset_server.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cglib.core.Local;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final ReservationRepository reservationRepository;
    private final ChargingStationRepository chargingStationRepository;

    /**
     * 1. 지난달, 충전소별 매출 집계
     */
    public TotalStatResponse getMonthlyTotalReport(YearMonth month) {

        Pair<LocalDate, LocalDate> findMonth = TimeUtils.getMonthlyRangeByMonth(month);
        Pair<LocalDate, LocalDate> lastMonth = TimeUtils.getMonthlyRangeByMonth(month.minusMonths(1));

        List<ChargingStat> thisResults = transactionRepository.getChargingStatsReport(findMonth.getFirst(), findMonth.getSecond());
        List<ChargingStat> lastResults = transactionRepository.getChargingStatsReport(lastMonth.getFirst(), lastMonth.getSecond());

        // 1. map 변환
        Map<String, ChargingStat> findMonthStats = thisResults.stream()
                .collect(Collectors.toMap(ChargingStat::getStationId, stat -> stat));
        Map<String, ChargingStat> previousMonthStats = lastResults.stream()
                .collect(Collectors.toMap(ChargingStat::getStationId, stat -> stat));

        // 2. 이번달 저번달 충전 정보가 있는 Set
        Set<String> allStationIds = findAllStationIds();

        List<StationStatReport> stationStatReports = allStationIds.stream()
                .map(stationId -> createStationStatReport(stationId, findMonthStats, previousMonthStats)).toList();

        return createTotalStatResponse(stationStatReports, allStationIds, findMonth);
    }

    /**
     * 2. 보고서 엑셀 생성
     */
    public Resource createExcelReport(YearMonth month) throws IOException {

        Pair<LocalDate, LocalDate> reportMonth = TimeUtils.getMonthlyRangeByMonth(month);

        try (Workbook workbook = new XSSFWorkbook()) {

            // 1. 총 매출 집계 (이전달 비교) 시트 추가
            createSummarySheet(month, workbook);

            // 2. 이번달 전체 충전 데이터 시트 추가
            createTransactionSheet(workbook, reportMonth);

            // 3. 이번달 전체 예약 데이터 시트 추가
            createReservationSheet(workbook, reportMonth);

            // 시트 반환
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        }


    }

    private void createSummarySheet(YearMonth month, Workbook workbook) {

        Sheet sheet = workbook.createSheet("총 매출 요약");
        TotalStatResponse stat = getMonthlyTotalReport(month);

        CellStyle boldStyle = SheetStyleUtils.createBoldStyle(sheet.getWorkbook());
        CellStyle percentStyle = SheetStyleUtils.createPercentStyle(sheet.getWorkbook());
        CellStyle moneyStyle = SheetStyleUtils.createMoneyStyle(sheet.getWorkbook());

        setReportTitle(sheet, "월간 충전소 보고서", stat.getFromDate(), stat.getToDate(), 4);
        int rowIdx = 3;

        Cell subTitle1 = sheet.createRow(rowIdx++).createCell(1);
        subTitle1.setCellValue("[전체 충전소 통계]");
        subTitle1.setCellStyle(boldStyle);
        sheet.addMergedRegion(new CellRangeAddress(3,3,1,2));

        Row summaryRow1 = sheet.createRow(rowIdx++);
        summaryRow1.createCell(1).setCellValue("충전소 수");
        summaryRow1.createCell(2).setCellValue(stat.getStationCount());
        summaryRow1.createCell(4).setCellValue("총 매출");
        summaryRow1.createCell(5).setCellValue(stat.getTotalRevenue());

        Row summaryRow2 = sheet.createRow(rowIdx++);

        summaryRow2.createCell(1).setCellValue("총 충전 횟수");
        summaryRow2.createCell(2).setCellValue(stat.getTotalCount());
        summaryRow2.createCell(4).setCellValue("매출 성장률 (%)");
        Cell rateCell = summaryRow2.createCell(5);
        rateCell.setCellValue(stat.getRevenueGrowthRate() / 100.0);
        rateCell.setCellStyle(percentStyle);

        Row summaryRow3 = sheet.createRow(rowIdx++);
        summaryRow3.createCell(1).setCellValue("총 에너지 (Wh)");
        summaryRow3.createCell(2).setCellValue(stat.getTotalEnergy());

        sheet.createRow(rowIdx++); // 줄 바꿈
        sheet.createRow(rowIdx++); // 줄 바꿈

        // ▶ 충전소별 상세 테이블 헤더
        Cell subTitle2 = sheet.createRow(rowIdx++).createCell(1);
        subTitle2.setCellValue("[전체 충전소 통계]");
        subTitle2.setCellStyle(boldStyle);
        sheet.addMergedRegion(new CellRangeAddress(8,8,1,2));

        Row header = sheet.createRow(rowIdx++);
        String[] headers = {"충전소 ID", "총 충전 횟수", "총 매출 (₩)", "총 에너지 (kWh)", "전월 매출 (₩)", "매출 성장률 (%)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i + 1);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(boldStyle);
        }

        // ▶ 충전소별 상세 데이터
        for (StationStatReport r : stat.getStationChargingStats()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(1).setCellValue(r.getStationId());
            row.createCell(2).setCellValue(r.getTotalCount());

            Cell revenueCell = row.createCell(3);
            revenueCell.setCellValue(r.getTotalRevenue());
            revenueCell.setCellStyle(moneyStyle);

            row.createCell(4).setCellValue((double) r.getTotalEnergy() / 1000.0);

            Cell lastMonthCell = row.createCell(5);
            lastMonthCell.setCellValue(r.getTotalRevenueLastMonth());
            lastMonthCell.setCellStyle(moneyStyle);

            Cell growthCell = row.createCell(6);
            growthCell.setCellValue(r.getRevenueGrowthRate() / 100.0);
            growthCell.setCellStyle(percentStyle);
        }

        // 자동 너비 조정
        for (int i = 0; i <= 9; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createReservationSheet(Workbook workbook, Pair<LocalDate, LocalDate> reportMonth) {

        Sheet sheet = workbook.createSheet("예약 이력");
        CellStyle boldStyle = SheetStyleUtils.createBoldStyle(sheet.getWorkbook());

        // 보고서 제목 설정
        setReportTitle(sheet, "충전소별 월간 예약 이력", reportMonth.getFirst(), reportMonth.getSecond(), 3);

        // 예약 리스트 작성
        int rowIdx = 3;
        rowIdx = setReservationInfos(sheet, "ST-001", reportMonth, rowIdx, boldStyle);
        sheet.createRow(rowIdx++);
        sheet.createRow(rowIdx++);
        setReservationInfos(sheet, "ST-002", reportMonth, rowIdx, boldStyle);

        // 자동 너비 조정
        for (int i = 0; i <= 9; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private int setReservationInfos(Sheet sheet, String stationId, Pair<LocalDate, LocalDate> reportMonth, int rowIdx, CellStyle boldStyle) {

        List<ReservationInfoResponse> reservations = reservationRepository.findAllReservationsByStationIdAndStartTime(stationId, reportMonth.getFirst(), reportMonth.getSecond());

        Cell subTitle2 = sheet.createRow(rowIdx).createCell(1);
        subTitle2.setCellValue("[" + stationId + " 예약 이력]");
        subTitle2.setCellStyle(boldStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx,1,2));
        rowIdx++;

        Row header = sheet.createRow(rowIdx++);
        String[] headers = {"예약 번호", "EVSE ID", "유저 ID", "예약 시작 시간", "예약 종료 시간", "예약 상태"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i + 1);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(boldStyle);
        }

        // ▶ 충전소별 상세 데이터
        for (ReservationInfoResponse r : reservations) {
            int col = 1;
            Row row = sheet.createRow(rowIdx++);
            row.createCell(col++).setCellValue(r.getId());
            row.createCell(col++).setCellValue(r.getEvseId());
            row.createCell(col++).setCellValue(r.getUserId());
            row.createCell(col++).setCellValue(r.getStartTime());
            row.createCell(col++).setCellValue(r.getEndTime());
            row.createCell(col).setCellValue(r.getReservationStatus());
        }
        return rowIdx;
    }


    private void createTransactionSheet(Workbook workbook, Pair<LocalDate, LocalDate> reportMonth) {
        Sheet sheet = workbook.createSheet("충전 이력");

        CellStyle boldStyle = SheetStyleUtils.createBoldStyle(sheet.getWorkbook());

        // 보고서 제목 설정
        setReportTitle(sheet, "충전소별 월간 충전 이력", reportMonth.getFirst(), reportMonth.getSecond(), 3);

        // 예약 리스트 작성
        int rowIdx = 3;
        rowIdx = setTransactionInfos(sheet, "ST-001", reportMonth, rowIdx, boldStyle);
        sheet.createRow(rowIdx++);
        sheet.createRow(rowIdx++);
        setTransactionInfos(sheet, "ST-002", reportMonth, rowIdx, boldStyle);

        // 자동 너비 조정
        for (int i = 0; i <= 9; i++) {
            sheet.autoSizeColumn(i);
        }
    }


    private int setTransactionInfos(Sheet sheet, String stationId, Pair<LocalDate, LocalDate> reportMonth, int rowIdx, CellStyle boldStyle) {

        List<TransactionInfoResponse> transactions = transactionRepository.findAllTransactionByStationIdAndEndTime(stationId, reportMonth.getFirst(), reportMonth.getSecond());

        CellStyle moneyStyle = SheetStyleUtils.createMoneyStyle(sheet.getWorkbook());

        Cell subTitle2 = sheet.createRow(rowIdx).createCell(1);
        subTitle2.setCellValue("[" + stationId + " 충전 이력]");
        subTitle2.setCellStyle(boldStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx,1,2));
        rowIdx++;

        Row header = sheet.createRow(rowIdx++);
        String[] headers = {"충전 번호", "EVSE ID", "유저 ID", "충전량 (kWh)", "충전 비용 (₩)", "예약 시작 시간", "예약 종료 시간", "예약 상태"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i + 1);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(boldStyle);
        }


        // ▶ 충전소별 상세 데이터
        for (TransactionInfoResponse t : transactions) {
            int col = 1;
            Row row = sheet.createRow(rowIdx++);
            row.createCell(col++).setCellValue(t.getId());
            row.createCell(col++).setCellValue(t.getEvseId());
            row.createCell(col++).setCellValue(t.getUserId());
            row.createCell(col++).setCellValue((double) t.getEnergyWh() / 1000.0);

            Cell costCell = row.createCell(col++);
            costCell.setCellValue(t.getCost());
            costCell.setCellStyle(moneyStyle);

            row.createCell(col++).setCellValue(t.getStartTime());
            row.createCell(col++).setCellValue(t.getEndTime());
            row.createCell(col).setCellValue(t.getTransactionStatus().toString());
        }
        return rowIdx;
    }

    private static void setReportTitle(Sheet sheet, String title, LocalDate reportStartDate, LocalDate reportEndDate, int column) {
        CellStyle titleStyle = SheetStyleUtils.createTitleStyle(sheet.getWorkbook());
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(20);

        Cell titleCell = titleRow.createCell(column);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0,0,column,column + 2));

        Row periodRow = sheet.createRow(1);
        periodRow.createCell(column + 4).setCellValue("기간");
        periodRow.createCell(column + 5).setCellValue(reportStartDate + " ~ " + reportEndDate);
        sheet.createRow(2); // 줄 바꿈
    }


    //== static 메서드 ==//
    private static TotalStatResponse createTotalStatResponse(List<StationStatReport> stationStatReports, Set<String> allStationIds, Pair<LocalDate, LocalDate> findMonth) {

        long totalCount = 0;
        long totalEnergy = 0;
        long totalRevenue = 0;
        long totalRevenueLastMonth = 0;

        for (StationStatReport report : stationStatReports) {
            totalCount += report.getTotalCount();
            totalEnergy += report.getTotalEnergy();
            totalRevenue += report.getTotalRevenue();
            totalRevenueLastMonth += report.getTotalRevenueLastMonth();
        }

        return new TotalStatResponse(
                allStationIds.size(),
                findMonth.getFirst(),
                findMonth.getSecond(),
                stationStatReports,
                totalCount,
                totalEnergy,
                totalRevenue,
                totalRevenueLastMonth
        );
    }

    private static StationStatReport createStationStatReport(String stationId, Map<String, ChargingStat> findMonthMap, Map<String, ChargingStat> lastMonthMap) {
        ChargingStat currentStat = findMonthMap.getOrDefault(stationId, new ChargingStat(stationId, 0, 0, 0));
        ChargingStat previousStat = lastMonthMap.getOrDefault(stationId, new ChargingStat(stationId, 0, 0, 0));

        return new StationStatReport(
                stationId,
                currentStat.getTotalCount(),
                currentStat.getTotalEnergy(),
                currentStat.getTotalRevenue(),
                previousStat.getTotalRevenue()
        );
    }

    private Set<String> findAllStationIds() {
        return chargingStationRepository.findAll().stream()
                .map(ChargingStation::getStationId)
                .collect(Collectors.toSet());
    }
}
