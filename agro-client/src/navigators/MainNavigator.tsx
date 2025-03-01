import { createNativeStackNavigator } from '@react-navigation/native-stack';
import React from 'react';
import TabNavigator from './TabNavigator';
import EditProfileScreen from '../screens/home/EditProfileScreen';
import ProductDetailScreen from '../screens/home/ProductDetailScreen';
import { LoginScreen } from '../screens';
import ChangePasswordScreen from '../screens/home/ChangePasswordScreen';
const Stack = createNativeStackNavigator();
import ForgotPasswordScreen from '../screens/auth/ForgotPasswordScreen';
import ResetPasswordScreen from '../screens/auth/ResetPasswordScreen';
import OtpVerificationScreen from '../screens/auth/OtpVerificationScreen';
import SignupScreen from '../screens/auth/SignupScreen';

const MainNavigator = () => {
    return (
        <Stack.Navigator screenOptions={{ headerShown: false }}>
            {/* đăng nhập xong mới vào đây -> đăng nhập xong mới dùng được các dưới đây */}
            <Stack.Screen name='Main' component={TabNavigator} />
            <Stack.Screen name='EditProfile' component={EditProfileScreen} />
            <Stack.Screen name='ChangePassword' component={ChangePasswordScreen} />
            <Stack.Screen name='ProductDetailScreen' component={ProductDetailScreen} />
            <Stack.Screen name='LoginScreen' component={LoginScreen} />
            <Stack.Screen name='SignupScreen' component={SignupScreen} />
            <Stack.Screen name='ForgotPasswordScreen' component={ForgotPasswordScreen} />
            <Stack.Screen name='ResetPasswordScreen' component={ResetPasswordScreen} />
            <Stack.Screen name='OtpVerificationScreen' component={OtpVerificationScreen} />
        </Stack.Navigator>
    );
};

export default MainNavigator;
