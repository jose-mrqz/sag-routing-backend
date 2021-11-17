package pe.sag.routing.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.sag.routing.api.request.NewOrderRequest;
import pe.sag.routing.core.model.Order;
import pe.sag.routing.core.scheduler.OrderScheduler;
import pe.sag.routing.data.parser.OrderParser;
import pe.sag.routing.data.repository.OrderRepository;
import pe.sag.routing.shared.dto.OrderDto;
import pe.sag.routing.shared.util.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.pow;
import static java.time.temporal.ChronoUnit.DAYS;

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
        ArrayList<Order> orders = new ArrayList<>();
        for(OrderDto od : orderRequest){
            Order order = OrderParser.fromDto(od);

            //asign code
            Order lastOrder = findFirstByOrderByCodeDesc();
            int code = 0;
            if (lastOrder == null) code++;
            else code = lastOrder.getCode()+1;
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
        if (status == OrderStatus.PROGRAMADO) order.setDemandGLP(0.0);
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
        return orderRepository.findFirst200ByStatusAndMonitoringOrderByRegistrationDateAscDeadlineDateAsc(status, isMonitoring);
    }

    public Order cancelOrder(String id, double amount) {
        Optional<Order> orderOptional = orderRepository.findBy_id(id);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setDemandGLP(amount + order.getDemandGLP());
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
        order.setDeadlineDate(order.getRegistrationDate().plusHours(request.getSlack()));
        return orderRepository.save(order);
    }

    public int deleteByCode(int code) {
        return orderRepository.deleteByCode(code);
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

    public ArrayList<OrderDto> generateFutureOrders(LocalDateTime startDate,LocalDateTime endDate){
        List<Integer> coeficients = generateCoeficients();
        int a = coeficients.get(0), b = coeficients.get(1), n = coeficients.get(2);

        ArrayList<OrderDto> futureOrders = new ArrayList<>();
        LocalDateTime orderDate = LocalDateTime.of(startDate.toLocalDate(),startDate.toLocalTime());//falta variacion de horas, min, seg
        long totalDates = DAYS.between(startDate, endDate);
        for(int numberDate = 1; numberDate <= totalDates; numberDate++){
            //FALTA: confirmar si se calcula cantidad de pedidos por dia y si x es el numero de dia que mandan
            int cantOrders = futureOrdersFunction(numberDate, a, b, n);
            for(int i = 0; i < cantOrders; i++){
                //FALTA: saber como generar estos atributos (fecha de llegada de pedido,
                //ubicación de entrega, cantidad de GLP y número de horas límite)
                int x = 10, y = 10, totalDemand = 5, slack = 4;
                int demandGLP = 0;
                //ArrayList<> variationDate;

                OrderDto orderDto = OrderDto.builder()
                        .x(x)
                        .y(y)
                        .demandGLP(demandGLP)
                        .totalDemand(totalDemand)
                        .registrationDate(orderDate)
                        .deadlineDate(orderDate.plusHours(slack))
                        .build();

                //FALTA: confirmar si reviso bloqueo aqui o en simulacion
                //Revisar si nodo de pedido se encuentra bloqueado
                /*if(!orderDto.inRoadblocks(roadblocks)){
                    futureOrders.add(orderDto);
                }*/
                futureOrders.add(orderDto);
            }
            orderDate = orderDate.plusDays(1);
        }
        return futureOrders;
    }

    public List<Integer> generateCoeficients(){
        List<Integer> coeficients = new ArrayList<>();

        //FALTA: saber si son escogidos a criterio nuestro o debemos calcularlo (como?)
        int a = 1, b = 1, n = 1;

        coeficients.add(a);
        coeficients.add(b);
        coeficients.add(n);

        return coeficients;
    }

    public int futureOrdersFunction(int x, int a, int b, int n){
        //FALTA: saber con exactitud que funcion polinomial es
        return (int) (a*pow(x,n) + b);//modificar con funcion lineal
    }
}
