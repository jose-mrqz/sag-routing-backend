package pe.sag.routing.core.service;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.sag.routing.api.request.NewOrderRequest;
import pe.sag.routing.core.model.FutureOrdersGenerator;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.scheduler.OrderScheduler;
import pe.sag.routing.data.parser.OrderParser;
import pe.sag.routing.data.repository.OrderRepository;
import pe.sag.routing.shared.dto.OrderDto;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.pow;
import static java.time.temporal.ChronoUnit.*;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderScheduler orderScheduler;

    public Order register(OrderDto orderRequest, boolean monitoring) throws IllegalAccessException {
        Order order = OrderParser.fromDto(orderRequest);

        //asign code
        Order lastOrder = findFirstByOrderByCodeDesc();
        int code = 0;
        if (lastOrder == null) code++;
        else code = lastOrder.getCode()+1;
        order.setCode(code);
        order.setTotalDemand(order.getDemandGLP());

        order.setStatus(OrderStatus.PENDIENTE);
        order.setMonitoring(monitoring);
        order.setDeliveryDate(null);
        return orderRepository.save(order);
    }

    public List<Order> registerAll(ArrayList<OrderDto> orderRequest, boolean monitoring) throws IllegalAccessException {
        Order lastOrder = findFirstByOrderByCodeDesc();
        int code = 0;

        ArrayList<Order> orders = new ArrayList<>();
        for(OrderDto od : orderRequest){
            Order order = OrderParser.fromDto(od);

            //asign code
            if (code == 0){
                if (lastOrder == null) code++;
                else code = lastOrder.getCode()+1;
            }
            else code++;

            order.setCode(code);

            order.setStatus(OrderStatus.PENDIENTE);
            order.setMonitoring(monitoring);
            order.setDeliveryDate(null);

            orders.add(order);
        }
        return orderRepository.saveAll(orders);
    }

    public void updateStatus(Order order, OrderStatus status){
        order.setStatus(status);
        if (status == OrderStatus.PROGRAMADO) order.setTotalDemand(0.0);
        orderRepository.save(order);
    }

    public List<OrderDto> list(String filter, LocalDateTime startDate, LocalDateTime endDate) {
        if(startDate == null){
            startDate = LocalDateTime.of(2000,1,1,0,0,0);
        }
        if(endDate == null){
            endDate = LocalDateTime.of(2100,1,1,0,0,0);
        }

        if(filter.equals("todos")){
            return orderRepository.findByMonitoringAndRegistrationDateBetweenOrderByCodeAsc(true,
                    startDate, endDate).stream().map(OrderParser::toDto).collect(Collectors.toList());
        }
        else {
            OrderStatus status;
            if (filter.equals("pendiente")) {
                status = OrderStatus.PENDIENTE;
            } else if (filter.equals("programado")) {
                status = OrderStatus.PROGRAMADO;
            } else {
                status = OrderStatus.ENTREGADO;
            }
            return orderRepository.findByStatusAndMonitoringAndRegistrationDateBetweenOrderByCodeAsc(status,
                true, startDate, endDate).stream().map(OrderParser::toDto).collect(Collectors.toList());
        }
    }

    public List<Order> listPendingsMonitoring(boolean monitoring) {
        return orderRepository.findByStatusAndMonitoringOrderByRegistrationDateAsc(OrderStatus.PENDIENTE,monitoring);
    }

    public Order findByCode(int code) {
        return orderRepository.findByCode(code).orElse(null);
    }

    public Order findFirstByOrderByCodeDesc() throws IllegalAccessException {
        Optional<Order> lastOrder = orderRepository.findFirstByOrderByCodeDesc();
        return lastOrder.orElse(null);
    }

    public Order findById(String id) {
        Optional<Order> order = orderRepository.findBy_id(id);
        return order.orElse(null);
    }

    public void deleteByMonitoring(boolean monitoring) {
        orderRepository.deleteByMonitoring(monitoring);
    }

    public void scheduleStatusChange(String id, OrderStatus status, LocalDateTime now) {
        orderScheduler.scheduleStatusChange(id, status, now);
    }

    public List<Order> getBatchedByStatusMonitoring(OrderStatus status, boolean isMonitoring) {
        if (isMonitoring) return orderRepository.findFirst80ByStatusAndMonitoringAndDeadlineDateAfterOrderByRegistrationDateAscDeadlineDateAsc(status, isMonitoring, LocalDateTime.now());
        return orderRepository.findFirst500ByStatusAndMonitoringOrderByRegistrationDateAscDeadlineDateAsc(status, isMonitoring);
    }

    public Order cancelOrder(String id, double amount) {
        Optional<Order> orderOptional = orderRepository.findBy_id(id);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setTotalDemand(amount + order.getDemandGLP());
            orderScheduler.cancelStatusChange(id);
            order.setStatus(OrderStatus.PENDIENTE);
            order.setDeliveryDate(null);
            return orderRepository.save(order);
        } else return null;
    }

    public Order edit(Order order, NewOrderRequest request) {
        order.setX(request.getX());
        order.setY(request.getY());
        order.setDemandGLP(request.getDemandGLP());
        order.setTotalDemand(request.getDemandGLP());
        order.setDeadlineDate(order.getRegistrationDate().plusHours(request.getSlack()));
        return orderRepository.save(order);
    }

    public int deleteByCode(int code) {
        return orderRepository.deleteByCode(code);
    }

    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByMonitoringAndRegistrationDateBetweenOrderByCodeAsc(true, startDate, endDate);
    }

    public void registerDeliveryDate(List<Order> orders, List<pe.sag.routing.algorithm.Order> ordersAlgorithm){
        List<pe.sag.routing.algorithm.Order> ordersAlgorithmLeft = new ArrayList<>(ordersAlgorithm);
        for(Order o : orders){
            for(pe.sag.routing.algorithm.Order oa : ordersAlgorithmLeft){
                if( oa.get_id().compareTo(o.get_id()) == 0 ){
                    o.setDeliveryDate(oa.getDeliveryTime());
                    ordersAlgorithmLeft.remove(oa);
                    break;
                }
            }
        }
        orderRepository.saveAll(orders);
    }

    public ArrayList<OrderDto> generateFutureOrders(LocalDateTime startDate, LocalDateTime endDate){
        ArrayList<OrderDto> futureOrders = new ArrayList<>();

        //Generar coeficicientes y parametro de crecimiento
        int a = 5, b = 240;
        double n = 1.223;

        LocalDateTime orderDate = LocalDateTime.of(startDate.toLocalDate(),startDate.toLocalTime());
        long totalDates = DAYS.between(startDate, endDate);
        for(int numberDate = 1; numberDate <= totalDates; numberDate++){
            int maxGLP = (int) (a*pow(numberDate,n) + b); //revisar como cambia esto

            FutureOrdersGenerator futureOrdersGenerator = new FutureOrdersGenerator();
            ArrayList<OrderDto> futureOrdersDay = futureOrdersGenerator.generateFutureOrders(maxGLP, orderDate);
            futureOrders.addAll(futureOrdersDay);

            orderDate = orderDate.plusDays(1);
            //if(numberDate==20)break;//
        }
        return futureOrders;
    }

    public List<FileWriter> generateFile(ArrayList<OrderDto> futureOrders) throws IOException {
        String projectPath = System.getProperty("user.dir");
        FileWriter fileWriter = null;
        PrintWriter printWriter = null;
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd:HH:mm");
        NumberFormat formatter = new DecimalFormat("#0.0");
        List<FileWriter> files = new ArrayList<>();

        // clear dir
        File dir = new File(projectPath + "/files/orders");
        FileUtils.cleanDirectory(dir);

        int lastMonth = 0;
        for (OrderDto orderDto : futureOrders) {
            int actualMonth = orderDto.getRegistrationDate().getMonthValue();
            if(actualMonth != lastMonth){
                if(lastMonth != 0) {
                    printWriter.close();
                    files.add(fileWriter);
                }

                int actualYear = orderDto.getRegistrationDate().getYear();
                String actualMonthString;
                if(actualMonth<10) actualMonthString = "0" + actualMonth;
                else actualMonthString = Integer.toString(actualMonth);

                fileWriter = new FileWriter(projectPath + "/files/orders/" + "ventas" + actualYear + actualMonthString + ".txt",
                        false);
                printWriter = new PrintWriter(fileWriter);
                lastMonth = actualMonth;
//                fileWriter.close();
//                printWriter.close();
            }

            int x = orderDto.getX();
            int y = orderDto.getY();
            int tw = (int) HOURS.between(orderDto.getRegistrationDate(), orderDto.getDeadlineDate());
            double demand = orderDto.getDemandGLP();
            LocalDateTime day = orderDto.getRegistrationDate();
            printWriter.print(day.format(format) + ",");
            printWriter.print(x + "," + y + ",");
            printWriter.println(formatter.format(demand) + "," + tw);
        }
        if(printWriter != null){
            printWriter.close();
            files.add(fileWriter);
        }
        return files;
    }
}
