package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StateMachineConfigTest {

    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Test
    public void testNewStateMachine() {
        StateMachine<PaymentState, PaymentEvent> sm = factory.getStateMachine(UUID.randomUUID());
        sm.start();
        sm.sendEvent(PaymentEvent.PRE_AUTHORIZE);
        assertEquals(PaymentState.NEW, sm.getState().getId());
        sm.sendEvent(PaymentEvent.PRE_AUTH_APPROVED);
        assertEquals(PaymentState.PRE_AUTH, sm.getState().getId());
        //no se puede probar la siguiente transición porque no ha vuelto a NEW

    }
}