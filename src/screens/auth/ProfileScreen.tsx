import React, { useState } from 'react';
import { View, Image, StyleSheet, TextInput, Alert, Modal, Button, TouchableOpacity } from 'react-native';
import { launchImageLibrary, ImageLibraryOptions, Asset } from 'react-native-image-picker';
import { ContainerComponent, SectionComponent, TextComponent, SpaceComponent, ButtonComponent } from '../../components';
import { Call, Sms, Calendar1, Camera } from 'iconsax-react-native';
import { appColors } from '../../constants/appColors';

const ProfileScreen = ({ route }: any) => {
    const { fullName, birthDate, phoneNumber, email, avatar } = route.params;

    const [isEditing, setIsEditing] = useState(false);
    const [newFullName, setNewFullName] = useState(fullName);
    const [newBirthDate, setNewBirthDate] = useState(birthDate);
    const [newPhoneNumber, setNewPhoneNumber] = useState(phoneNumber);
    const [newEmail, setNewEmail] = useState(email);
    const [newAvatar, setNewAvatar] = useState(avatar);
    
    const [otpModalVisible, setOtpModalVisible] = useState(false);
    const [otp, setOtp] = useState('');

    // Chọn ảnh từ thư viện
    const pickImage = () => {
        const options: ImageLibraryOptions = {
            mediaType: 'photo', // Đúng kiểu dữ liệu
            quality: 1,
            maxWidth: 500,
            maxHeight: 500,
        };
    
        launchImageLibrary(options, (response) => {
            if (response.didCancel) {
                console.log('Người dùng đã hủy chọn ảnh');
            } else if (response.errorCode) {
                console.log('Lỗi khi chọn ảnh:', response.errorMessage);
            } else if (response.assets && response.assets.length > 0) {
                const selectedImage: Asset = response.assets[0]; // Kiểm tra chắc chắn có ảnh
                setNewAvatar(selectedImage.uri);
            }
        });
    };

    // Lưu thông tin
    const handleSave = () => {
        if (newPhoneNumber !== phoneNumber || newEmail !== email) {
            setOtpModalVisible(true);
        } else {
            setIsEditing(false);
            Alert.alert("Thông báo", "Thông tin đã được cập nhật!");
        }
    };

    // Xác nhận OTP
    const verifyOtp = () => {
        if (otp === "123456") { // OTP demo
            setOtpModalVisible(false);
            setIsEditing(false);
            Alert.alert("Thông báo", "Xác nhận OTP thành công! Thông tin đã được lưu.");
        } else {
            Alert.alert("Lỗi", "Mã OTP không đúng!");
        }
    };

    return (
        <ContainerComponent isScroll>
            {/* Phần Avatar & Tên */}
            <SectionComponent styles={styles.center}>
                <TouchableOpacity onPress={pickImage}>
                    <Image source={{ uri: newAvatar }} style={styles.avatar} />
                    <View style={styles.cameraIcon}>
                        <Camera size={20} color={appColors.white} />
                    </View>
                </TouchableOpacity>
                <SpaceComponent height={16} />
                {isEditing ? (
                    <TextInput
                        style={styles.input}
                        value={newFullName}
                        onChangeText={setNewFullName}
                    />
                ) : (
                    <TextComponent size={26} text={newFullName} title />
                )}
            </SectionComponent>

            <SpaceComponent height={20} />

            {/* Thông tin chi tiết */}
            <View style={styles.infoContainer}>
                <View style={styles.infoRow}>
                    <Calendar1 size={22} color={appColors.primary} />
                    {isEditing ? (
                        <TextInput
                            style={styles.input}
                            value={newBirthDate}
                            onChangeText={setNewBirthDate}
                        />
                    ) : (
                        <TextComponent text={`Ngày sinh: ${newBirthDate}`} />
                    )}
                </View>

                <SpaceComponent height={12} />

                <View style={styles.infoRow}>
                    <Call size={22} color={appColors.primary} />
                    {isEditing ? (
                        <TextInput
                            style={styles.input}
                            value={newPhoneNumber}
                            onChangeText={setNewPhoneNumber}
                            keyboardType="phone-pad"
                        />
                    ) : (
                        <TextComponent text={`Số điện thoại: ${newPhoneNumber}`} />
                    )}
                </View>

                <SpaceComponent height={12} />

                <View style={styles.infoRow}>
                    <Sms size={22} color={appColors.primary} />
                    {isEditing ? (
                        <TextInput
                            style={styles.input}
                            value={newEmail}
                            onChangeText={setNewEmail}
                            keyboardType="email-address"
                        />
                    ) : (
                        <TextComponent text={`Email: ${newEmail}`} />
                    )}
                </View>
            </View>

            <SpaceComponent height={20} />

            {/* Nút Chỉnh sửa & Lưu */}
            <SectionComponent>
                {isEditing ? (
                    <ButtonComponent text="Lưu" type="primary" onPress={handleSave} />
                ) : (
                    <ButtonComponent text="Chỉnh sửa" type="primary" onPress={() => setIsEditing(true)} />
                )}
            </SectionComponent>

            {/* Modal nhập OTP */}
            <Modal visible={otpModalVisible} animationType="slide" transparent>
                <View style={styles.modalContainer}>
                    <View style={styles.modalContent}>
                        <TextComponent size={20} text="Nhập mã OTP" title />
                        <SpaceComponent height={12} />
                        <TextInput
                            style={styles.otpInput}
                            value={otp}
                            onChangeText={setOtp}
                            keyboardType="numeric"
                            maxLength={6}
                            placeholder="Nhập OTP"
                        />
                        <SpaceComponent height={16} />
                        <Button title="Xác nhận" onPress={verifyOtp} />
                    </View>
                </View>
            </Modal>
        </ContainerComponent>
    );
};

// 🎨 **Styles**
const styles = StyleSheet.create({
    center: {
        alignItems: 'center',
        marginTop: 50,
    },
    avatar: {
        width: 130,
        height: 130,
        borderRadius: 65,
        borderWidth: 3,
        borderColor: appColors.primary,
    },
    cameraIcon: {
        position: 'absolute',
        bottom: 0,
        right: 10,
        backgroundColor: appColors.primary,
        padding: 6,
        borderRadius: 20,
    },
    infoContainer: {
        backgroundColor: appColors.white,
        padding: 16,
        borderRadius: 12,
        marginHorizontal: 16,
        shadowColor: '#000',
        shadowOpacity: 0.1,
        shadowOffset: { width: 0, height: 4 },
        shadowRadius: 6,
        elevation: 3,
    },
    infoRow: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
    },
    input: {
        flex: 1,
        borderBottomWidth: 1,
        borderBottomColor: appColors.gray,
        fontSize: 16,
        paddingVertical: 4,
    },
    modalContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: 'rgba(0,0,0,0.5)',
    },
    modalContent: {
        backgroundColor: 'white',
        padding: 20,
        borderRadius: 10,
        alignItems: 'center',
    },
    otpInput: {
        width: 150,
        borderBottomWidth: 2,
        borderBottomColor: appColors.primary,
        fontSize: 20,
        textAlign: 'center',
    },
});

export default ProfileScreen;
